package it.unimi.cloudproject.ui.lambda;

import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.HttpStatus;
import it.unimi.cloudproject.ui.testcontainer.AppContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public class LambdaIT {
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build();

    @Container
    private static final AppContainer app = new AppContainer();

//    @Container
//    private static final PostgreSQLContainer<?> systemDb = new PostgreSQLContainer<>("postgres:15").withUsername("postgres")
//            .withPassword("postgres")
//            .withNetwork(tsvContainer.NETWORK)
//            .withCreateContainerCmdModifier((createContainerCmd) -> createContainerCmd.withName(DB_CONTAINER_NAME));
//
//    @Container
//    private static final ProviderContainer providerContainer = new ProviderContainer().withNetwork(tsvContainer.NETWORK)
//            .withCreateContainerCmdModifier(createContainerCmd -> createContainerCmd.withName(PROVIDER_CONTAINER_NAME)
//                    .withHostName(PROVIDER_CONTAINER_NAME.replace('_',
//                            '.')));
//
//    // @Container
//    // private static final S3ReplacementContainer s3ReplacementContainer = new
//    // S3ReplacementContainer().withNetwork(tsvContainer.NETWORK)
//    // .withCreateContainerCmdModifier(createContainerCmd ->
//    // createContainerCmd.withName(S3_FS_REPLACEMENT)
//    // .withHostName(S3_FS_REPLACEMENT.replace('_',
//    // '.')));
//
//    // Pgadmin container to check the postgres container while debugging
//    // @Container
//    // private static final GenericContainer<?> pgAdmin = new GenericContainer<>("dpage/pgadmin4:6")
//    // .withNetwork(tsvContainer.NETWORK)
//    // .withExposedPorts(80)
//    // .withEnv(new HashMap<>() {{
//    // put("PGADMIN_DEFAULT_EMAIL", "user@domain.com");
//    // put("PGADMIN_DEFAULT_PASSWORD", "SuperSecret");
//    // }});
//
    @BeforeAll
    static void initializeAll() throws IOException {
        app.initialize();
    }
//
//    @AfterEach
//    void cleanupEach()
//    {
//        tsvContainer.log();
//    }
//
    @Test
    void givenUserApi_whenRegister_thenIdIsReturned() throws IOException, InterruptedException
    {
//        var httpResponse = HTTP_CLIENT.send(HttpRequest.newBuilder()
//                        .POST(HttpRequest.BodyPublishers.ofString("""
//                 {
//                    "username": "test"
//                 }"""))
//                        .header("Content-Type", "application/json")
//                        .timeout(Duration.ofSeconds(100))
////                        .uri(new URI(app.getStartSFWorkflowUri()))
//                        .build(),
//                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
//
//        assertThat(httpResponse.statusCode()).isEqualTo(HttpStatus.SC_OK);
//        System.out.println(httpResponse.body());
    }
}
