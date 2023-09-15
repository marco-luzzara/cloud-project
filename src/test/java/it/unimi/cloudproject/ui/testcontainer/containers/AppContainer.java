package it.unimi.cloudproject.ui.testcontainer.containers;

import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Frame;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

public class AppContainer extends LocalStackContainer {
    public final String NETWORK_ALIAS = "localstack";
    private static final System.Logger LOGGER = System.getLogger(AppContainer.class.getName());
    public final Network NETWORK = Network.SHARED;
    private static final DockerImageName localstackImage = DockerImageName.parse("localstack/localstack-pro:latest");

    private String restApiId;
    private String deploymentStageName;

    private final boolean keepLambdasOpenedAfterExit;

    public AppContainer()
    {
        this(false);
    }

    public AppContainer(boolean keepLambdasOpenedAfterExit)
    {
        super(localstackImage);

        this.keepLambdasOpenedAfterExit = keepLambdasOpenedAfterExit;

        var apiKey = getApiKeyOrThrow();

        // https://joerg-pfruender.github.io/software/testing/2020/09/27/localstack_and_lambda.html#1-networking
        withNetwork(NETWORK);
        withNetworkAliases(NETWORK_ALIAS);
        // 4566 - standard port, 4510 - RDS port
        withExposedPorts(4510, 4566);
        withServices(
                Service.LAMBDA,
                Service.API_GATEWAY,
                Service.S3,
                LocalStackContainer.EnabledService.named("rds"));
        withEnv(Map.of(
                "LAMBDA_DOCKER_NETWORK", ((Network.NetworkImpl) NETWORK).getName(),
                "MAIN_DOCKER_NETWORK", ((Network.NetworkImpl) NETWORK).getName(),
                "LOCALSTACK_API_KEY", apiKey
                ));
    }

    private String getApiKeyOrThrow() {
        try {
            return new PathMatchingResourcePatternResolver()
                    .getResource("localstack/apikey.secret")
                    .getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Please create an accessible file called 'apikey.secret' with the Localstack API key in the folder src/test/resources/localstack");
        }
    }

    /**
     * run the terraform apply to create all the necessary resources
     */
    public void initialize(TerraformContainer terraform) {
        terraform.initialize();
        terraform.apply(new TerraformContainer.TfVariables(
                this.getAccessKey(),
                this.getSecretKey(),
                NETWORK_ALIAS,
                4566
        ));
        this.restApiId = terraform.getOutputVar(TerraformContainer.OutputVar.REST_API_ID);
        this.deploymentStageName = terraform.getOutputVar(TerraformContainer.OutputVar.DEPLOYMENT_STAGE_NAME);

        this.followOutput(outFrame ->
                LOGGER.log(System.Logger.Level.INFO,
                        "%s - %s".formatted(outFrame.getType(), outFrame.getUtf8String())));
    }

    public void logAndPossiblyDestroyLambda() {
        var thisNetworkId = Objects.requireNonNull(this.getNetwork()).getId();
        // lambda containers are not removed after the test because spawned by the localstack
        // container, and not directly by testcontainers. use docker api to
        // remove all lambda containers connected to the same network as localstack

        final String LAMBDA_IMAGE = "public.ecr.aws/lambda/java";
        dockerClient.listContainersCmd()
                .exec()
                .stream()
                .filter(c -> Objects.requireNonNull(c.getNetworkSettings())
                        .getNetworks()
                        .values()
                        .stream()
                        .anyMatch(network -> Objects.equals(network.getNetworkID(),
                                thisNetworkId))
                        && c.getImage().startsWith(LAMBDA_IMAGE))
                .forEach(c ->
                {
                    var containerName = dockerClient.inspectContainerCmd(c.getId())
                            .exec()
                            .getName();
                    var sb = new StringBuilder("Logs from %s%n".formatted(containerName));
                    sb.append("**************************").append(System.lineSeparator());
                    try {
                        dockerClient.logContainerCmd(c.getId())
                                .withStdOut(true)
                                .withStdErr(true)
                                .withTailAll()
                                .exec(new ResultCallback.Adapter<>() {
                                    @Override
                                    public void onNext(Frame frame) {
                                        sb.append(new String(frame.getPayload()));
                                    }
                                }).awaitCompletion();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    sb.append("**************************").append(System.lineSeparator());
                    LOGGER.log(System.Logger.Level.INFO, sb.toString());

                    if (!keepLambdasOpenedAfterExit) {
                        dockerClient.stopContainerCmd(c.getId()).exec();
                        dockerClient.removeContainerCmd(c.getId()).exec();
                    }
                });
    }

    public URI buildApiUrl(String pathPart) {
        Objects.requireNonNull(this.restApiId);

// http://localhost:4566/restapis/$REST_API_ID/$DEPLOYMENT_NAME/_user_request_/{pathPart}
        return URI.create("%s/restapis/%s/%s/_user_request_/%s".formatted(
                this.getEndpoint(),
                this.restApiId,
                this.deploymentStageName,
                pathPart));
    }
}
