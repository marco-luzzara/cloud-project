package it.unimi.cloudproject.ui.testcontainer.model;

import org.testcontainers.shaded.org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class LocalstackGlobals {

    private static Properties globals = new Properties();

    static {
        try (var resourceStream = LocalstackGlobals.class.getClassLoader()
                .getResourceAsStream("localstack/scripts/globals.env")) {
             globals.load(resourceStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private LocalstackGlobals() {}

    public static String getRegion() {
        return globals.getProperty("_GLOBALS_REGION");
    }

    public static String getDistS3Bucket() {
        return globals.getProperty("_GLOBALS_DIST_S3_BUCKET");
    }

    public static String getDistS3Key() {
        return globals.getProperty("_GLOBALS_DIST_S3_KEY");
    }

    public static String getRoleArn() {
        return globals.getProperty("_GLOBALS_ROLE_ARN");
    }

    public static String getRestApiName() {
        return globals.getProperty("_GLOBALS_REST_API_NAME");
    }

    public static String getDeploymentName() {
        return globals.getProperty("_GLOBALS_DEPLOYMENT_NAME");
    }
}
