package it.unimi.cloudproject.ui.testcontainer;

import org.junit.jupiter.api.Assertions;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public class AppContainer extends LocalStackContainer {
//    private final Path awsSetupVariablesFile = createTempFile();
//
//    private final Map<String, String> awsSetupVariables = new HashMap<>();

    public final Network NETWORK = Network.SHARED;
    private static final DockerImageName localstackImage = DockerImageName.parse("localstack/localstack:2.2.0");

    private String startSFWorkflowUri;

    private boolean keepLambdasOpenedAfterExit;

    public AppContainer()
    {
        this(false);
    }

    public AppContainer(boolean keepLambdasOpenedAfterExit)
    {
        super(localstackImage);

        this.keepLambdasOpenedAfterExit = keepLambdasOpenedAfterExit;

        withServices(Service.LAMBDA, Service.API_GATEWAY, Service.S3);
        // https://joerg-pfruender.github.io/software/testing/2020/09/27/localstack_and_lambda.html#1-networking
        withNetwork(NETWORK);
        withExposedPorts(4566);
        withEnv(Map.of("LAMBDA_DOCKER_NETWORK",
                ((Network.NetworkImpl) NETWORK).getName(),
                "MAIN_DOCKER_NETWORK",
                ((Network.NetworkImpl) NETWORK).getName()));
    }

    /**
     * create the following tree in the localstack container
    /app/
        dist.zip
        scripts/
            setup.sh
            utils/
                s3-utils.sh

     * Moreover it executes the script to reproduce the aws environment (by creating lambda and so on)
     * @throws IOException
     */
    public void initialize() throws IOException {
        var projectFolder = Paths.get(".").toAbsolutePath();
        var distDir = projectFolder.resolve("dist");
        var zipPath = Files.list(distDir).findFirst().orElseThrow(
                () -> new IllegalStateException("you must produce a zip file containing the lambda code before running IT tests"));

        copyFileInsideContainer(zipPath, "/app", "dist.zip");
        // script utils
        copyFileInsideContainer(getPathFromResourceId("localstack/scripts/utils/s3-utils.sh"), "/app/scripts/utils");

        setupAwslocal("access_key", "secret_key", "us-east-1");


//
//        final var workflowFile = getPathFromResourceId("it/addvalue/tsv/stepfunctions/workflow.json");
//        final var awsSetupScriptPath = getPathFromResourceId("it/addvalue/tsv/sh/aws-setup.sh");
//        final var awsLogScriptPath = getPathFromResourceId("it/addvalue/tsv/sh/aws-get-last-logs.sh");
//
//        copyFileInsideContainer(jarFile, "/app", "setup.zip");
//        copyFileInsideContainer(workflowFile, "/app");
//        copyFileInsideContainer(awsSetupScriptPath, "/scripts");
//        copyFileInsideContainer(awsLogScriptPath, "/scripts");
//
//        executeScriptInsideContainer("/scripts/aws-setup.sh");
//        initializeAwsSetupVariables();
//        startSFWorkflowUri = "http://localhost:%d/restapis/%s/%s/_user_request_/%s".formatted(getFirstMappedPort(),
//                awsSetupVariables.get("REST_API_ID"),
//                awsSetupVariables.get("DEPLOYMENT_NAME"),
//                awsSetupVariables.get("WORKFLOW_PATH_PART"));
//
//        this.followOutput(outFrame ->
//        {
//            LOGGER.log(System.Logger.Level.INFO,
//                    "%s - %s".formatted(outFrame.getType(), outFrame.getUtf8String()));
//        });
    }

    private void setupAwslocal(String accessKeyId, String secretKey, String region) {
        copyFileInsideContainer(getPathFromResourceId("localstack/scripts/setup.sh"), "/app/scripts");
        executeScriptInsideContainer("/app/scripts/setup.sh", Map.of(
                "_ACCESS_KEY_ID", accessKeyId,
                "_SECRET_KEY_ID", secretKey,
                "_REGION", region
        ));
    }

    private static Path getPathFromResourceId(String resourceId)
    {
        try {
            return Path.of(Objects.requireNonNull(AppContainer.class.getClassLoader()
                    .getResource(resourceId)).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

//    public void log()
//    {
//        // for complete logs we may use
//        // https://joerg-pfruender.github.io/software/testing/2020/09/27/localstack_and_lambda.html#3-logging
//        executeScriptInsideContainer("/scripts/aws-get-last-logs.sh");
//    }

    private void copyFileInsideContainer(Path file, String containerBasePath)
    {
        copyFileInsideContainer(file, containerBasePath, file.getFileName().toString());
    }

    private void copyFileInsideContainer(Path file,
                                         String containerBasePath,
                                         String containerFilename)
    {
        final var containerPath = containerBasePath + '/' + containerFilename;
        copyFileToContainer(MountableFile.forHostPath(file, 777), containerPath);
    }

    private String executeScriptInsideContainer(String scriptPathInContainer, Map<String, String> params)
    {
        ExecResult scriptResult;
        try
        {
            var sb = new StringBuilder();
            for (var param : params.entrySet())
                sb.append("%s=\"%s\" ".formatted(param.getKey(), param.getValue()));
            sb.append(scriptPathInContainer);
            scriptResult = execInContainer("sh", "-c", sb.toString());
        }
        catch (IOException | InterruptedException e)
        {
            throw new RuntimeException(e);
        }

        checkScriptSuccessful(scriptPathInContainer, scriptResult);
        return getScriptReturnValue(scriptResult.getStdout());
    }

    /**
     * get only the last line of the stdout produced by a script execution
     * @param stdout
     * @return the last line of script output
     */
    private String getScriptReturnValue(String stdout) {
        var scriptOutput = stdout.split("\n");
        return scriptOutput[scriptOutput.length - 1];
    }

    static private void checkScriptSuccessful(String scriptName, ExecResult execResult)
    {
        if (execResult.getExitCode() != 0)
            throw new IllegalStateException("""
                    The script %s returned with exit code %d
                    %s""".formatted(scriptName, execResult.getExitCode(), execResult.getStderr()));
    }
    
    @Override
    public void stop()
    {
        // store the network before stopping it. it is used later if child lambdas must be stopped
        // as well
        var thisNetworkId = Objects.requireNonNull(this.getNetwork()).getId();

        super.stop();

        var dockerClient = DockerClientFactory.instance().client();

        // lambda containers are not removed after the test because spawned by the localstack
        // container, and not directly by testcontainers. use docker api to
        // remove all lambda containers connected to the same network as localstack
        if (!keepLambdasOpenedAfterExit)
        {
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
                        dockerClient.stopContainerCmd(c.getId()).exec();
                        dockerClient.removeContainerCmd(c.getId()).exec();
                    });
        }
    }
}
