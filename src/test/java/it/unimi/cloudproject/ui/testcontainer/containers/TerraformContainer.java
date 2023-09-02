package it.unimi.cloudproject.ui.testcontainer.containers;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.nio.file.Path;

import java.lang.System.Logger;

public class TerraformContainer extends GenericContainer<TerraformContainer> {
    private static final Logger logger = System.getLogger(TerraformContainer.class.getName());
    private static final String IMAGE = "hashicorp/terraform:1.5.6";
    private final TfVariables initializationVariables;

    public TerraformContainer(Path terraformScriptsPath, TfVariables initializationVariables) {
        super(DockerImageName.parse(IMAGE));

        this.withWorkingDirectory("/app")
                // sleep is the entrypoint, otherwise the container expect a terraform command and exits immediately
                .withCreateContainerCmdModifier(createContainerCmd -> createContainerCmd.withEntrypoint("sh", "-c", "sleep inf"))
                .withFileSystemBind(terraformScriptsPath.toAbsolutePath().toString(), "/app");
        this.initializationVariables = initializationVariables;
    }

    public void apply() {
        try {
            this.execInContainer("terraform", "init");

            var optionVarsAssigned = buildOptionForVarsAssigning();
            var planCmdExecution = this.execInContainer("sh", "-c", "terraform plan %s".formatted(optionVarsAssigned));
            assert planCmdExecution.getExitCode() == 0 : planCmdExecution.getStderr();
            logger.log(Logger.Level.INFO, planCmdExecution.getStdout());

            this.execInContainer("sh", "-c", "terraform apply -auto-approve %s".formatted(optionVarsAssigned));
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String buildOptionForVarsAssigning() {
        var varFormat = "-var=\"%s=%s\"";
        return String.join(" ",
                varFormat.formatted("aws_access_key", this.initializationVariables.accessKey()),
                varFormat.formatted("aws_secret_key", this.initializationVariables.secretKey()),
                varFormat.formatted("localstack_hostname", this.initializationVariables.localstackHostname()),
                varFormat.formatted("localstack_port", this.initializationVariables.localstackPort()));
    }

    public record TfVariables(String accessKey,
                              String secretKey,
                              String localstackHostname,
                              int localstackPort) {}
}
