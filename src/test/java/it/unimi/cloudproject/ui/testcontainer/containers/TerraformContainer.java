package it.unimi.cloudproject.ui.testcontainer.containers;

import com.google.gson.Gson;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.io.IOException;
import java.lang.System.Logger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TerraformContainer extends GenericContainer<TerraformContainer> {
    private static final Logger logger = System.getLogger(TerraformContainer.class.getName());
    private static final String IMAGE = "hashicorp/terraform:1.5.6";
    private Map<String, Object> outputVars = new HashMap<>();

    public TerraformContainer() {
        super(DockerImageName.parse(IMAGE));

        this.withWorkingDirectory("/app")
                // sleep is the entrypoint, otherwise the container expect a terraform command and exits immediately
                .withCreateContainerCmdModifier(createContainerCmd -> createContainerCmd.withEntrypoint("sh", "-c", "sleep inf"));
    }

    public void initialize() {
        try {
            var projectFolder = Paths.get(".").toAbsolutePath();
            var distDir = projectFolder.resolve("dist");
            var zipPath = Files.list(distDir).findFirst().orElseThrow(
                    () -> new IllegalStateException("you must produce a zip file containing the lambda code before its creation"));
            this.copyFileToContainer(MountableFile.forHostPath(zipPath, 444), "/app/dist.zip");
            copyTerraformFilesToContainer();

            this.execInContainer("terraform", "init");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void apply(TfVariables tfVariables) {
        try {
            createTfOverrideFileForLocalstackProvider(tfVariables);

            var applyCmdExecution = this.execInContainer("sh", "-c", "terraform apply -auto-approve");
            assertContainerCmdSuccessful(applyCmdExecution);

            this.populateOutputVarFromTerraform();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public enum OutputVar {
        REST_API_ID("webapp_apigw_rest_api_id"),
        DEPLOYMENT_STAGE_NAME("webapp_apigw_stage_name")
        ;

        private final String varName;
        OutputVar(String varName) {
            this.varName = varName;
        }
    }

    public String getOutputVar(OutputVar outputVar) {
        return ((Map<String, Object>) this.outputVars.get(outputVar.varName)).get("value").toString();
    }

    private void assertContainerCmdSuccessful(ExecResult execResult) {
        assert execResult.getExitCode() == 0 : execResult.getStderr();
        logger.log(Logger.Level.INFO, execResult.getStdout());
    }

    private void populateOutputVarFromTerraform() throws IOException, InterruptedException {
        var outputVarJson = this.execInContainer("terraform", "output", "-json").getStdout();
        var gson = new Gson();
        this.outputVars = gson.fromJson(outputVarJson, this.outputVars.getClass());
    }

    private void createTfOverrideFileForLocalstackProvider(TfVariables tfVariables) {
        var providerOverrideFileContent = """
                provider "aws" {
                  access_key                  = "%1$s"
                  secret_key                  = "%2$s"
                  region                      = var.aws_region
                  s3_use_path_style           = true
                  skip_credentials_validation = true
                  skip_metadata_api_check     = true
                  skip_requesting_account_id  = true
                                
                  endpoints {
                    apigateway     = "%3$s"
                    apigatewayv2   = "%3$s"
                    cloudformation = "%3$s"
                    cloudwatch     = "%3$s"
                    dynamodb       = "%3$s"
                    ec2            = "%3$s"
                    es             = "%3$s"
                    elasticache    = "%3$s"
                    firehose       = "%3$s"
                    iam            = "%3$s"
                    kinesis        = "%3$s"
                    lambda         = "%3$s"
                    rds            = "%3$s"
                    redshift       = "%3$s"
                    route53        = "%3$s"
                    s3             = "%3$s"
                    secretsmanager = "%3$s"
                    ses            = "%3$s"
                    sns            = "%3$s"
                    sqs            = "%3$s"
                    ssm            = "%3$s"
                    stepfunctions  = "%3$s"
                    sts            = "%3$s"
                  }
                }
                """.formatted(tfVariables.accessKey(),
                    tfVariables.secretKey(),
                    "http://%s:%s".formatted(tfVariables.localstackHostname(),
                            tfVariables.localstackPort()));
        this.copyFileToContainer(Transferable.of(providerOverrideFileContent), "/app/provider_override.tf");
    }

    /**
     * copy the terraform files in the container, with base path "/app".
     * The directory structure will be:
     * /app
     * - main.tf
     * - module1/
     * - - main.tf
     * - - variables.tf
     * - - localstack.auto.tfvars
     * ...
     */
    private void copyTerraformFilesToContainer() throws IOException {
        // all .tf files are copied, but only the .tfvars files for the Localstack env are included
        var terraformFiles = new PathMatchingResourcePatternResolver().getResources("classpath*:terraform/**/*.tf");
        var terraformVars = new PathMatchingResourcePatternResolver().getResources("classpath*:terraform/**/localstack*.auto.tfvars");

        var terraformResources = new ArrayList<Resource>();
        terraformResources.addAll(Arrays.stream(terraformFiles).toList());
        terraformResources.addAll(Arrays.stream(terraformVars).toList());

        terraformResources.stream().map(tr -> {
            try {
                var path = tr.getURL().getPath();
                var fromResourcesPathIndex = path.lastIndexOf("/terraform/");
                return path.substring(fromResourcesPathIndex + 1); // + 1 to exclude the leading /
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).forEach(tfResourcePath -> { // I receive paths like terraform/module/file.tf
            this.copyFileToContainer(MountableFile.forClasspathResource(tfResourcePath),
                    "/app" + tfResourcePath.substring("terraform".length()));
        });
    }

//    private ExecResult execInContainerWithLogs(String cmd) throws IOException, InterruptedException {
//        return this.execInContainer("sh", "-c", cmd + " &> /proc/1/fd/1");
//    }

    public record TfVariables(String accessKey,
                              String secretKey,
                              String localstackHostname,
                              int localstackPort) {}
}
