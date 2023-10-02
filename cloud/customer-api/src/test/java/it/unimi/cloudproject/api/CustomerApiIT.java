package it.unimi.cloudproject.api;

import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.HttpStatus;
import it.unimi.cloudproject.lambda.customer.dto.requests.user.UserCreationRequest;
import it.unimi.cloudproject.lambda.customer.dto.requests.user.UserLoginRequest;
import it.unimi.cloudproject.testcontainer.containers.AppContainer;
import it.unimi.cloudproject.testcontainer.containers.TerraformContainer;
import it.unimi.cloudproject.api.callers.CustomerApiCaller;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

//@EnabledIfSystemProperty(named = "IntegrationTestsEnabled", matches = "true")
@EnabledIfSystemProperty(named = "IntegrationTestsEnabled", matches = "true")
@Testcontainers
public class CustomerApiIT {
    @Container
//    private static final AppContainer app = new AppContainer();
    private static final AppContainer app = new AppContainer(new AppContainer.LocalstackConfig(false, "trace"));

    @Container
    private static final TerraformContainer terraform = new TerraformContainer().withNetwork(app.NETWORK);

    private final CustomerApiCaller userRestApiCaller = new CustomerApiCaller(app);

//    private static final String DB_CONTAINER_NAME = "localstack_db";

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
    static void initializeAll() throws IOException, InterruptedException {
        app.initialize(terraform);
    }

    @AfterEach
    void cleanupEach() throws IOException, InterruptedException {
        app.printCloudwatchLogs();
    }

    @AfterAll
    static void cleanupAll() throws IOException, InterruptedException {
        app.storeDiagnoseReportIfTracing();
        app.logAndPossiblyDestroyLambda();
    }

    @Test
    void successful_flow_userCreate_login_getUserInfo_userDelete() throws IOException, InterruptedException
    {
        final var username = "test@amazon.com";
        final var password = "testtest";
        var userCreation = new UserCreationRequest(username, password);

        // create user
        var userCreateResponse = userRestApiCaller.callUserCreateApi(userCreation);
        var id = userCreateResponse.body().id();
        assertThat(userCreateResponse.statusCode()).isEqualTo(HttpStatus.SC_OK);
        assertThat(id).isGreaterThanOrEqualTo(1);

        // login
        var userLoginResponse = userRestApiCaller.callUserLoginApi(new UserLoginRequest(username, password));
        assertThat(userLoginResponse.statusCode()).isEqualTo(HttpStatus.SC_OK);
        var idToken = userLoginResponse.body().idToken();
        assertThat(userLoginResponse.body().accessToken()).isNotEmpty();
        assertThat(idToken).isNotEmpty();

        // get user info
        var getUserInfoResponse = userRestApiCaller.callUserGetInfoApi(idToken);
        assertThat(getUserInfoResponse.statusCode()).isEqualTo(HttpStatus.SC_OK);
        assertThat(getUserInfoResponse.body().id()).isEqualTo(id);
        assertThat(getUserInfoResponse.body().username()).isEqualTo(username);

        // delete user
        var deleteUserResponse = userRestApiCaller.callUserDeleteApi(idToken);
        assertThat(deleteUserResponse.statusCode()).isEqualTo(HttpStatus.SC_OK);

        // get user info
        var getUserInfoAfterDeleteResponse = userRestApiCaller.callUserGetInfoApi(idToken);
        assertThat(getUserInfoAfterDeleteResponse.statusCode()).isEqualTo(HttpStatus.SC_NOT_FOUND);
    }
}
