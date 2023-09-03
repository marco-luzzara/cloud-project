package it.unimi.cloudproject.ui.lambda;

import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.HttpStatus;
import it.unimi.cloudproject.ui.dto.requests.user.UserCreationRequest;
import it.unimi.cloudproject.ui.testcontainer.containers.AppContainer;
import it.unimi.cloudproject.ui.testcontainer.containers.TerraformContainer;
import it.unimi.cloudproject.ui.testcontainer.helpers.LocalstackUserRestApiCaller;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public class LambdaIT {
    public static final String LOCALSTACK_HOSTNAME = "localstack";
    @Container
    private static final AppContainer app = new AppContainer();

    @Container
    private static final TerraformContainer terraform = new TerraformContainer(
            Path.of("src", "test", "resources", "terraform"),
            new TerraformContainer.TfVariables(
                    app.getAccessKey(),
                    app.getSecretKey(),
                    LOCALSTACK_HOSTNAME,
                    4566
            )
    );

    private final LocalstackUserRestApiCaller userRestApiCaller = new LocalstackUserRestApiCaller(app);

    private static final String DB_CONTAINER_NAME = "localstack_db";

//    @Container
//    private static final PostgreSQLContainer<?> localstackDb = DbFactory.getPostgresContainer()
//            .withNetwork(app.NETWORK)
//            .withCreateContainerCmdModifier((createContainerCmd) -> createContainerCmd.withName(DB_CONTAINER_NAME));

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
        terraform.initialize();
        terraform.apply();
//        app.initialize();
//        app.createApiForCreateUser();
//        app.createApiForDeleteUser();
//        app.createApiForGetUser();
//        app.completeSetup();
    }

    @AfterEach
    void cleanupEach()
    {
        app.logAndPossiblyDestroyLambda();
    }

    @Test
    void whenUserRegisterWithApi_thenIdIsReturned() throws IOException, InterruptedException
    {
        var userCreation = new UserCreationRequest("test");

        var httpResponse = userRestApiCaller.callUserCreateApi(userCreation);

        var id = httpResponse.body().id();
        assertThat(httpResponse.statusCode()).isEqualTo(HttpStatus.SC_OK);
        assertThat(id).isGreaterThanOrEqualTo(1);
        assertThat(userRestApiCaller.callUserGetApi(id).body().id()).isEqualTo(id);
    }

    @Test
    void givenUser_whenDeleteWithApi_thenCannotGetInfo() throws IOException, InterruptedException
    {
        var userCreation = new UserCreationRequest("test");
        var creationResponse = userRestApiCaller.callUserCreateApi(userCreation);
        var userId = creationResponse.body().id();

        var deletionResponse = userRestApiCaller.callUserDeleteApi(userId);

        assertThat(deletionResponse.statusCode()).isEqualTo(HttpStatus.SC_OK);
        assertThat(userRestApiCaller.callUserGetApi(userId).statusCode()).isEqualTo(404);
    }

    @Test
    void givenUser_whenAddShop_thenCannotGetInfo() throws IOException, InterruptedException
    {
        var userCreation = new UserCreationRequest("test");
        var creationResponse = userRestApiCaller.callUserCreateApi(userCreation);
        var userId = creationResponse.body().id();

        var deletionResponse = userRestApiCaller.callUserDeleteApi(userId);

        assertThat(deletionResponse.statusCode()).isEqualTo(HttpStatus.SC_OK);
        assertThat(userRestApiCaller.callUserGetApi(userId).statusCode()).isEqualTo(404);
    }
}
