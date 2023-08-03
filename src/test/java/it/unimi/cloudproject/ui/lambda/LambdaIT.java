package it.unimi.cloudproject.ui.lambda;

import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.HttpStatus;
import it.unimi.cloudproject.application.dto.UserCreationRequest;
import it.unimi.cloudproject.ui.testcontainer.AppContainer;
import it.unimi.cloudproject.ui.testcontainer.LocalstackRestApiCaller;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public class LambdaIT {
    @Container
    private static final AppContainer app = new AppContainer(true);

    private final LocalstackRestApiCaller apiCaller = new LocalstackRestApiCaller(app);

    private static final String DB_CONTAINER_NAME = "localstack_db";

    @Container
    private static final PostgreSQLContainer<?> localstackDb = new PostgreSQLContainer<>("postgres:15")
            .withUsername("sa")
            .withPassword("password")
            .withNetwork(app.NETWORK)
            .withDatabaseName("testdb")
            .withCreateContainerCmdModifier((createContainerCmd) -> createContainerCmd.withName(DB_CONTAINER_NAME));

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
        app.createApiForCreateUser();
        app.completeSetup();
    }

    @AfterEach
    void cleanupEach()
    {
        app.log();
    }

    @Test
    void givenUserApi_whenRegister_thenIdIsReturned() throws IOException, InterruptedException
    {
        var userCreation = new UserCreationRequest("test");

        var httpResponse = apiCaller.callUserCreateApi(userCreation);

        assertThat(httpResponse.statusCode()).isEqualTo(HttpStatus.SC_OK);
        assertThat(httpResponse.body().id()).isGreaterThanOrEqualTo(1);
    }
}
