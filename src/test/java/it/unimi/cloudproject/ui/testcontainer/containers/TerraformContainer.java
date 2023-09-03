package it.unimi.cloudproject.ui.testcontainer.containers;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.io.IOException;
import java.nio.file.Path;

import java.lang.System.Logger;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class TerraformContainer extends GenericContainer<TerraformContainer> {
    private static final Logger logger = System.getLogger(TerraformContainer.class.getName());
    private static final String IMAGE = "hashicorp/terraform:1.5.6";
    private final TfVariables initializationVariables;

    public TerraformContainer(Path terraformScriptsPath, TfVariables initializationVariables) {
        super(DockerImageName.parse(IMAGE));

        this.withWorkingDirectory("/app")
                // sleep is the entrypoint, otherwise the container expect a terraform command and exits immediately
                .withCreateContainerCmdModifier(createContainerCmd -> createContainerCmd.withEntrypoint("sh", "-c", "sleep inf"));
        this.initializationVariables = initializationVariables;
    }

    public void initialize() {
        try {
            this.copyFileToContainer(MountableFile.forClasspathResource("terraform/tf_helper.sh", 775),
                    "/app/tf_helper.sh");
            copyTerraformFilesToContainer();

            this.execInContainer("terraform", "init");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void apply() {
        try {
            createTfvarsFileForLocalstackProvider();
            var planCmdExecution = this.execInContainer("sh", "-c", "./tf_helper.sh -o plan -e localstack");
            assert planCmdExecution.getExitCode() == 0 : planCmdExecution.getStderr();
            logger.log(Logger.Level.INFO, planCmdExecution.getStdout());

            this.execInContainer("sh", "-c", "./tf_helper.sh -o apply -e localstack");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void createTfvarsFileForLocalstackProvider() {
        String sb =
                "aws_access_key = " + this.initializationVariables.accessKey() + System.lineSeparator() +
                "aws_secret_key = " + this.initializationVariables.secretKey() + System.lineSeparator() +
                "localstack_hostname = " + this.initializationVariables.localstackHostname() + System.lineSeparator() +
                "localstack_port = " + this.initializationVariables.localstackPort();
        this.copyFileToContainer(Transferable.of(sb), "/app/localstack.tfvars");
    }

    /**
     * copy the terraform files in the container, with base path "/app".
     * The directory structure will be:
     * /app
     * - main.tf
     * - module1/
     * - - main.tf
     * - - variables.tf
     * - - localstack.tfvars
     * ...
     * @throws IOException
     */
    private void copyTerraformFilesToContainer() throws IOException {
        var terraformFiles = new PathMatchingResourcePatternResolver().getResources("classpath*:terraform/**/*.tf");
        var terraformVars = new PathMatchingResourcePatternResolver().getResources("classpath*:terraform/**/*.tfvars");

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

    public record TfVariables(String accessKey,
                              String secretKey,
                              String localstackHostname,
                              int localstackPort) {}
}
