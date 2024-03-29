package it.unimi.cloudproject.testcontainer.containers;

import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Frame;
import it.unimi.cloudproject.testcontainer.helpers.AssertionHelper;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class AppContainer extends LocalStackContainer {
    public final String NETWORK_ALIAS = "localstack";
    private static final System.Logger LOGGER = System.getLogger(AppContainer.class.getName());
    public final Network NETWORK = Network.SHARED;
    private static final DockerImageName localstackImage = DockerImageName.parse("localstack/localstack-pro:3.0.2");

    private String restApiId;
    private String deploymentStageName;

    private final LocalstackConfig localstackConfig;

    private static final String GET_LOGS_FROM_CW_SCRIPT_NAME = "aws-get-last-logs.sh";

    public AppContainer()
    {
        this(new LocalstackConfig(false, "info"));
    }

    public AppContainer(LocalstackConfig localstackConfig)
    {
        super(localstackImage);

        this.localstackConfig = localstackConfig;

        var localstackEnv = new HashMap<>(Map.of(
                "LAMBDA_DOCKER_NETWORK", ((Network.NetworkImpl) NETWORK).getName(),
                "LS_LOG", this.localstackConfig.logLevel,
                "ENFORCE_IAM", "1"
        ));
        if (System.getenv("LOCALSTACK_API_KEY") != null)
            localstackEnv.put("LOCALSTACK_API_KEY", System.getenv("LOCALSTACK_API_KEY"));
        else
            localstackEnv.put("LOCALSTACK_AUTH_TOKEN", Optional.ofNullable(System.getenv("LOCALSTACK_AUTH_TOKEN")).orElseThrow());

        // https://joerg-pfruender.github.io/software/testing/2020/09/27/localstack_and_lambda.html#1-networking
        withNetwork(NETWORK);
        withNetworkAliases(NETWORK_ALIAS);
        // 4566 - standard port, 4510 - RDS port
        withExposedPorts(4510, 4566);
        withServices(
                Service.LAMBDA,
                Service.API_GATEWAY,
                Service.S3,
                Service.CLOUDWATCHLOGS,
                Service.SNS,
                Service.IAM,
                Service.SECRETSMANAGER,
                Service.EC2,
                Service.SES,
                LocalStackContainer.EnabledService.named("rds"),
                LocalStackContainer.EnabledService.named("cognito-idp"));
        withEnv(localstackEnv);
    }

    /**
     * run the terraform apply to create all the necessary resources
     */
    public void initialize(TerraformContainer terraform) throws IOException, InterruptedException {
        terraform.initialize();
        terraform.apply(new TerraformContainer.TfVariables(
                this.getAccessKey(),
                this.getSecretKey(),
                NETWORK_ALIAS,
                4566
        ));
        this.restApiId = terraform.getOutputVar(TerraformContainer.OutputVar.REST_API_ID);
        this.deploymentStageName = terraform.getOutputVar(TerraformContainer.OutputVar.DEPLOYMENT_STAGE_NAME);

        this.copyScriptToContainer("localstack/scripts/%s".formatted(GET_LOGS_FROM_CW_SCRIPT_NAME));

        this.followOutput(outFrame ->
                LOGGER.log(System.Logger.Level.INFO,
                        "%s - %s".formatted(outFrame.getType(), outFrame.getUtf8String())));
    }

    public void printCloudwatchLogs() throws IOException, InterruptedException {
        this.execScriptInContainer(GET_LOGS_FROM_CW_SCRIPT_NAME);
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

                    if (!localstackConfig.keepLambdasOpenedAfterExit()) {
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

    public URI buildBaseUrl(String urlPart) {
        return URI.create("%s/%s".formatted(this.getEndpoint(), urlPart));
    }

    public void storeDiagnoseReportIfTracing() throws IOException, InterruptedException {
        if (this.localstackConfig.logLevel().equals("trace")) {
            var reportHostPath = Paths.get(".").toAbsolutePath()
                    .resolve("build")
                    .resolve("reports")
                    .resolve("localstack_diagnose_report.json.gz");
            var reportContainerPath = "/diagnose.json.gz";
            // create diagnose report
            var diagnoseReportCmd = this.execInContainer("bash", "-c", "curl -s localhost:4566/_localstack/diagnose | gzip -cf > %s".formatted(reportContainerPath));
            AssertionHelper.assertContainerCmdSuccessful(diagnoseReportCmd);
            this.copyFileFromContainer(reportContainerPath, reportHostPath.toString());
        }
    }

    private void copyScriptToContainer(String scriptResourcePath) throws IOException, InterruptedException {
        var scriptResource = new PathMatchingResourcePatternResolver().getResource(scriptResourcePath);
        this.copyFileToContainer(Transferable.of(scriptResource.getContentAsByteArray()), "/" + scriptResource.getFilename());
        this.execInContainer("chmod", "+x", "/" + scriptResource.getFilename());
    }

    private void execScriptInContainer(String scriptName) throws IOException, InterruptedException {
        var executeScriptCmd = this.execInContainer("/" + scriptName);
        AssertionHelper.assertContainerCmdSuccessful(executeScriptCmd);
    }

    public record LocalstackConfig(boolean keepLambdasOpenedAfterExit,
                                   String logLevel) {}
}
