package it.unimi.cloudproject.ui.testcontainer.helpers;

import it.unimi.cloudproject.ui.testcontainer.containers.TerraformContainer;
import org.testcontainers.containers.Container;

public class TestContainerHelper {
    private static final System.Logger logger = System.getLogger(TestContainerHelper.class.getName());

    public static void assertContainerCmdSuccessful(Container.ExecResult execResult) {
        assert execResult.getExitCode() == 0 : execResult.getStderr();
        logger.log(System.Logger.Level.INFO, execResult.getStdout());
    }
}
