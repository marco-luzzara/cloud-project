package it.unimi.cloudproject.ui.testcontainer.containers;

import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Frame;
import com.google.gson.Gson;
import it.unimi.cloudproject.ui.lambda.LambdaIT;
import it.unimi.cloudproject.ui.testcontainer.model.LocalstackGlobals;
import it.unimi.cloudproject.ui.testcontainer.model.SetupScriptResults;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

public class AppContainer extends LocalStackContainer {
    private static final System.Logger LOGGER = System.getLogger(AppContainer.class.getName());

    public final Network NETWORK = Network.SHARED;
    private static final DockerImageName localstackImage = DockerImageName.parse("localstack/localstack-pro:2.2.0");

    private String restApiId;
    private String apiUsersResourceId;
    private String apiUsersWithIdResourceId;
    private String apiShopsResourceId;
    private String routingLambdaArn;
    private static final String ROUTING_LAMBDA_NAME = "routingLambda";
    private static final Gson gson = new Gson();

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

        withCreateContainerCmdModifier(cc -> cc.withHostName(LambdaIT.LOCALSTACK_HOSTNAME));
        withServices(Service.LAMBDA, Service.API_GATEWAY, Service.S3);
        // https://joerg-pfruender.github.io/software/testing/2020/09/27/localstack_and_lambda.html#1-networking
        withNetwork(NETWORK);
        withExposedPorts(4566);
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
     * create the following tree in the localstack container
    /app/
        dist.zip
        scripts/
            setup.sh
            ...
            utils/
                s3-utils.sh
                ...

     * Moreover it executes the script to reproduce the aws environment (by creating lambda and so on)
     * @throws IOException
     */
    public void initialize() throws IOException {
        // copy zip file to container
        var projectFolder = Paths.get(".").toAbsolutePath();
        var distDir = projectFolder.resolve("dist");
        var zipPath = Files.list(distDir).findFirst().orElseThrow(
                () -> new IllegalStateException("you must produce a zip file containing the lambda code before running IT tests"));
        copyFileInsideContainer(zipPath, "/app", "dist.zip");

        // copy env variables to container /app
        copyFileInsideContainer(getPathFromResourceId("localstack/scripts/globals.env"), "/app/scripts");

        // copy scripts to container /app
        var scripts = new PathMatchingResourcePatternResolver()
                .getResources("classpath*:localstack/scripts/**/*.sh");

        for (var script : scripts) {
            var scriptId = script.getURI().toString();
            var scriptIdFromResources = "localstack" + scriptId.substring(scriptId.lastIndexOf("/scripts/"));
            copyFileInsideContainer(getPathFromResourceId(scriptIdFromResources),
                    "/app/" + scriptIdFromResources.substring("localstack/".length(), scriptIdFromResources.lastIndexOf("/")));
        }

        setupAwslocal(this.getAccessKey(), this.getSecretKey());
        createRoutingLambda();

        this.followOutput(outFrame ->
        {
            LOGGER.log(System.Logger.Level.INFO,
                    "%s - %s".formatted(outFrame.getType(), outFrame.getUtf8String()));
        });
    }

    // scripts methods

    private void setupAwslocal(String accessKeyId, String secretKey) {
        var setupResult = executeScriptInsideContainer("/app/scripts/setup.sh", Map.of(
                "_ACCESS_KEY_ID", accessKeyId,
                "_SECRET_KEY_ID", secretKey
        ), SetupScriptResults.SetupScript.class);

        this.restApiId = setupResult.restApiId();
        this.apiUsersResourceId = setupResult.apiUsersResourceId();
        this.apiUsersWithIdResourceId = setupResult.apiUsersWithIdResourceId();
        this.apiShopsResourceId = setupResult.apiShopsResourceId();
    }

    public void completeSetup() {
        Objects.requireNonNull(this.restApiId);

        executeScriptInsideContainer("/app/scripts/complete.sh", Map.of(
                "_REST_API_ID", this.restApiId
        ));
    }

    private void createRoutingLambda() {
        this.routingLambdaArn = executeScriptInsideContainer("/app/scripts/create-routing-lambda.sh",
                Map.of(
                        "_LAMBDA_NAME", ROUTING_LAMBDA_NAME
                ), String.class);
    }

    public void createApiForCreateUser() {
        integrationPreconditions();

        executeScriptInsideContainer("/app/scripts/create-lambda-integration.sh",
                Map.of(
                        "_ROUTING_LAMBDA_ARN", this.routingLambdaArn,
                        "_FUNCTION_NAME", "createUser",
                        "_RESOURCE_ID", this.apiUsersResourceId,
                        "_HTTP_METHOD", "POST",
                        "_REST_API_ID", this.restApiId
//                        "_REQUEST_TEMPLATES", """
//                                {
//                                    \\"application/json\\": \\"{\\
//                                        \\\\\\"method\\\\\\": \\\\\\"\\$context.httpMethod\\\\\\",\\
//                                        \\\\\\"body\\\\\\" : \\$input.json('\\$'),\\
//                                        \\\\\\"headers\\\\\\": {\\
//                                            #foreach(\\$param in \\$input.params().header.keySet())\\
//                                            \\\\\\"\\$param\\\\\\": \\\\\\"\\$util.escapeJavaScript(\\$input.params().header.get(\\$param))\\\\\\"\\
//                                            #if(\\$foreach.hasNext),#end\\
//                                            #end\\
//                                        }\\
//                                    }\\"
//                                }"""
//                            """
//                            {
//                                \\"application/json\\": \\"#set(\\$context.requestOverride.header.spring_cloud_function_definition = 'createUser')\\"
//                            }"""
                ));
    }

    public void createApiForDeleteUser() {
        integrationPreconditions();

        executeScriptInsideContainer("/app/scripts/create-lambda-integration.sh",
                Map.of(
                        "_ROUTING_LAMBDA_ARN", this.routingLambdaArn,
                        "_FUNCTION_NAME", "deleteUser",
                        "_RESOURCE_ID", this.apiUsersWithIdResourceId,
                        "_HTTP_METHOD", "DELETE",
                        "_REST_API_ID", this.restApiId,
                        "_REQUEST_TEMPLATES", """
                                {
                                    \\"application/json\\": \\"{\\
                                        \\\\\\"id\\\\\\": \\\\\\"\\$input.params('userId')\\\\\\"\\
                                    }\\"
                                }"""
                ));
    }

    public void createApiForGetUser() {
        integrationPreconditions();

        executeScriptInsideContainer("/app/scripts/create-lambda-integration.sh",
                Map.of(
                        "_ROUTING_LAMBDA_ARN", this.routingLambdaArn,
                        "_FUNCTION_NAME", "getUser",
                        "_RESOURCE_ID", this.apiUsersWithIdResourceId,
                        "_HTTP_METHOD", "GET",
                        "_REST_API_ID", this.restApiId,
                        "_REQUEST_TEMPLATES", """
                                {
                                    \\"application/json\\": \\"{\\
                                        \\\\\\"id\\\\\\": \\\\\\"\\$input.params('userId')\\\\\\"\\
                                    }\\"
                                }"""
                ));

        addErrorResponseForApi(this.apiUsersWithIdResourceId, "GET", 404, ".*No value present.*");
    }

    private void addErrorResponseForApi(String resourceId, String httpMethod, int statusCode, String regexErrorPattern) {
        executeScriptInsideContainer("/app/scripts/put-method-err-response.sh",
                Map.of(
                        "_REST_API_ID", this.restApiId,
                        "_RESOURCE_ID", resourceId,
                        "_HTTP_METHOD", httpMethod,
                        "_STATUS_CODE", String.valueOf(statusCode),
                        "_REGEX_ERROR_PATTERN", regexErrorPattern
                ));
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

    private void integrationPreconditions() {
        Objects.requireNonNull(this.restApiId);
        Objects.requireNonNull(this.apiUsersResourceId);
        Objects.requireNonNull(this.routingLambdaArn);
    }

    public URI buildApiUrl(String pathPart) {
        Objects.requireNonNull(this.restApiId);

//        http://localhost:4566/restapis/$REST_API_ID/$DEPLOYMENT_NAME/_user_request_/{pathPart}
        return URI.create("%s/restapis/%s/%s/_user_request_/%s".formatted(
                this.getEndpoint(),
                this.restApiId,
                LocalstackGlobals.getDeploymentName(),
                pathPart));
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

    private <T> T executeScriptInsideContainer(String scriptPathInContainer,
                                               Map<String, String> params,
                                               Class<T> returnValueClass)
    {
        ExecResult scriptResult;
        try
        {
            var sb = new StringBuilder();
            for (var param : params.entrySet())
                sb.append("%s=\"%s\" ".formatted(param.getKey(), param.getValue()));
            sb.append(scriptPathInContainer);
            scriptResult = execInContainer("bash", "-c", sb.toString());
        }
        catch (IOException | InterruptedException e)
        {
            throw new RuntimeException(e);
        }

        checkScriptSuccessful(scriptPathInContainer, scriptResult);

        return switch (returnValueClass.getSimpleName()) {
            case "Void" -> null;
            case "String" -> (T) getScriptReturnValue(scriptResult.getStdout());
            default -> {
                var scriptOutput = getScriptReturnValue(scriptResult.getStdout());
                yield this.gson.fromJson(scriptOutput, returnValueClass);
            }
        };
    }

    private void executeScriptInsideContainer(String scriptPathInContainer,
                                               Map<String, String> params) {
        executeScriptInsideContainer(scriptPathInContainer, params, Void.class);
    }

    /**
     * get only the last line of the stdout produced by a script execution
     * @param stdout the script output
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
}
