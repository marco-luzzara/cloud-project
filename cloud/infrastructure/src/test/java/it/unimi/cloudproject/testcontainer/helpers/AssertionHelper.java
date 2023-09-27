package it.unimi.cloudproject.testcontainer.helpers;

import org.testcontainers.containers.Container;

public class AssertionHelper {
    private static final System.Logger logger = System.getLogger(AssertionHelper.class.getName());

    public static void assertContainerCmdSuccessful(Container.ExecResult execResult) {
        assert execResult.getExitCode() == 0 : execResult.getStderr();
        logger.log(System.Logger.Level.INFO, execResult.getStdout());
    }
}
