package it.unimi.cloudproject.api;

import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.HttpStatus;
import it.unimi.cloudproject.api.callers.AdminApiCaller;
import it.unimi.cloudproject.api.callers.CustomerApiCaller;
import it.unimi.cloudproject.api.callers.LocalstackApiCaller;
import it.unimi.cloudproject.api.callers.ShopApiCaller;
import it.unimi.cloudproject.api.callers.dto.ShopPublishMessageRequestBody;
import it.unimi.cloudproject.helpers.api.CustomerApiHelper;
import it.unimi.cloudproject.helpers.factories.ShopDataFactory;
import it.unimi.cloudproject.helpers.factories.UserDataFactory;
import it.unimi.cloudproject.lambda.admin.dto.responses.ShopCreationResponse;
import it.unimi.cloudproject.lambda.customer.dto.requests.user.UserLoginRequest;
import it.unimi.cloudproject.lambda.customer.dto.responses.LoginResponse;
import it.unimi.cloudproject.lambda.customer.dto.responses.UserGetInfoResponse;
import it.unimi.cloudproject.testcontainer.containers.AppContainer;
import it.unimi.cloudproject.testcontainer.containers.TerraformContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS_ENABLED", matches = "true")
@Testcontainers
public class ApiIT {
    @Container
    private static final AppContainer app = new AppContainer();
    // to generate logs of type trace use this AppContainer initialization
//    private static final AppContainer app = new AppContainer(new AppContainer.LocalstackConfig(false, "trace"));

    @Container
    private static final TerraformContainer terraform = new TerraformContainer().withNetwork(app.NETWORK);

    private final CustomerApiCaller customerApiCaller = new CustomerApiCaller(app);
    private final ShopApiCaller shopApiCaller = new ShopApiCaller(app);
    private final AdminApiCaller adminApiCaller = new AdminApiCaller(app);
    private final LocalstackApiCaller localstackApiCaller = new LocalstackApiCaller(app);

    // Pgadmin container to check the postgres container while debugging
    // @Container
    // private static final GenericContainer<?> pgAdmin = new GenericContainer<>("dpage/pgadmin4:6")
    // .withNetwork(tsvContainer.NETWORK)
    // .withExposedPorts(80)
    // .withEnv(new HashMap<>() {{
    // put("PGADMIN_DEFAULT_EMAIL", "user@domain.com");
    // put("PGADMIN_DEFAULT_PASSWORD", "SuperSecret");
    // }});

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
    void successfulFlow_userCreate_login_getUserInfo_userDelete() throws IOException, InterruptedException
    {
        final var userCreation = UserDataFactory.getNewUser();

        // create user
        var newUserInfo = CustomerApiHelper.createUserAndLoginSuccessfully(customerApiCaller, userCreation);
        var id = newUserInfo.userId();
        var idToken = newUserInfo.idToken();

        // get user info
        var getUserInfoResponse = customerApiCaller.<UserGetInfoResponse>callUserGetInfoApi(idToken);
        assertThat(getUserInfoResponse.statusCode()).isEqualTo(HttpStatus.SC_OK);
        assertThat(getUserInfoResponse.body().id()).isEqualTo(id);
        assertThat(getUserInfoResponse.body().username()).isEqualTo(userCreation.username());

        // delete user
        var deleteUserResponse = customerApiCaller.callUserDeleteApi(idToken);
        assertThat(deleteUserResponse.statusCode()).isEqualTo(HttpStatus.SC_OK);

        // get user info
        var getUserInfoAfterDeleteResponse = customerApiCaller.callUserGetInfoApi(idToken);
        assertThat(getUserInfoAfterDeleteResponse.statusCode()).isEqualTo(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    void givenTwoShopOwner_whenShopOwnerSendsUsingDifferentShopId_thenThrow() throws IOException, InterruptedException {
        final var admin = UserDataFactory.getAdminUser();
        // admin login
        final var adminLoginResponse = customerApiCaller.<LoginResponse>callUserLoginApi(new UserLoginRequest(admin.username(), admin.password()));
        final var adminIdToken = adminLoginResponse.body().idToken();

        // create user 1
        final var userCreation1 = UserDataFactory.getNewUser();
        var newUserInfo1 = CustomerApiHelper.createUserAndLoginSuccessfully(customerApiCaller, userCreation1);
        var id1 = newUserInfo1.userId();
        var idToken1 = newUserInfo1.idToken();

        // create user 2
        final var userCreation2 = UserDataFactory.getNewUser();
        var newUserInfo2 = CustomerApiHelper.createUserAndLoginSuccessfully(customerApiCaller, userCreation2);
        var id2 = newUserInfo2.userId();
        var idToken2 = newUserInfo2.idToken();

        // create shop 1
        var shopCreationBody1 = ShopDataFactory.getNewShop(id1);
        var adminCreateShopApiResponse1 = adminApiCaller.<ShopCreationResponse>callAdminCreateShopApi(shopCreationBody1, adminIdToken);
        final var shopId1 = adminCreateShopApiResponse1.body().shopId();
        assertThat(shopId1).isGreaterThanOrEqualTo(1);

        // create shop 2
        var shopCreationBody2 = ShopDataFactory.getNewShop(id2);
        var adminCreateShopApiResponse2 = adminApiCaller.<ShopCreationResponse>callAdminCreateShopApi(shopCreationBody2, adminIdToken);
        final var shopId2 = adminCreateShopApiResponse2.body().shopId();
        assertThat(shopId2).isGreaterThanOrEqualTo(shopId1);

        var shopPublishMessageResponse = shopApiCaller.callShopPublishMessageApi(
                new ShopPublishMessageRequestBody("test message"), idToken1, shopId2);
        assertThat(shopPublishMessageResponse.statusCode()).isEqualTo(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    void givenUserSubscribedToShop_whenShopSendMessage_UserReceivesIt() throws IOException, InterruptedException
    {
        final var admin = UserDataFactory.getAdminUser();
        // admin login
        final var adminLoginResponse = customerApiCaller.<LoginResponse>callUserLoginApi(new UserLoginRequest(admin.username(), admin.password()));
        assertThat(adminLoginResponse.statusCode()).isEqualTo(HttpStatus.SC_OK);
        assertThat(adminLoginResponse.body().idToken()).isNotEmpty();
        final var adminIdToken = adminLoginResponse.body().idToken();

        // shop owner creation
        final var shopOwner = UserDataFactory.getNewUser();
        var shopOwnerInfo = CustomerApiHelper.createUserAndLoginSuccessfully(customerApiCaller, shopOwner);
        var ownerId = shopOwnerInfo.userId();
        var ownerIdToken = shopOwnerInfo.idToken();

        // customer creation
        final var customer = UserDataFactory.getNewUser();
        var customerInfo = CustomerApiHelper.createUserAndLoginSuccessfully(customerApiCaller, customer);
        var customerId = customerInfo.userId();
        var customerIdToken = customerInfo.idToken();

        // create shop
        var shopCreationBody = ShopDataFactory.getNewShop(ownerId);
        var adminCreateShopApiResponse = adminApiCaller.<ShopCreationResponse>callAdminCreateShopApi(shopCreationBody, adminIdToken);
        assertThat(adminCreateShopApiResponse.statusCode()).isEqualTo(HttpStatus.SC_OK);
        final var shopId = adminCreateShopApiResponse.body().shopId();
        assertThat(shopId).isGreaterThanOrEqualTo(1);

        // customer subscribe to shop
        var userSubscribeToShopResponse = customerApiCaller.callUserSubscribeToShopApi(customerIdToken, shopId);
        assertThat(userSubscribeToShopResponse.statusCode()).isEqualTo(HttpStatus.SC_OK);

        // shop publish a message
        var shopPublishMessageResponse = shopApiCaller.callShopPublishMessageApi(
                new ShopPublishMessageRequestBody("test message"), ownerIdToken, shopId);
        assertThat(shopPublishMessageResponse.statusCode()).isEqualTo(HttpStatus.SC_OK);

        // wait for the sns messages to be queriable
        Thread.sleep(5000);

        var sesResponseBody = localstackApiCaller.<Map<String, Object>>callGetEmailsApi(customer.username()).body();
        var messages = (List<Object>) sesResponseBody.get("messages");
        assertThat(messages).hasSizeGreaterThanOrEqualTo(1);
        var body = ((Map<String, Object>) messages.get(0)).get("Body");
        var textMessage = ((Map<String, Object>) body).get("text_part");
        assertThat(textMessage).isEqualTo("test message");

        // delete shop
        var shopDeleteResponse = shopApiCaller.callShopDeleteApi(ownerIdToken, shopId);
        assertThat(shopDeleteResponse.statusCode()).isEqualTo(HttpStatus.SC_OK);
    }
}
