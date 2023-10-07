package it.unimi.cloudproject.lambda.admin.configurations;

import it.unimi.cloudproject.apigw.message.model.InvocationWrapper;
import it.unimi.cloudproject.lambda.admin.dto.requests.ShopCreationRequest;
import it.unimi.cloudproject.lambda.admin.dto.responses.ShopCreationResponse;
import it.unimi.cloudproject.lambda.admin.errors.CannotCreateShop;
import it.unimi.cloudproject.services.dto.ShopCreation;
import it.unimi.cloudproject.services.services.ShopService;
import it.unimi.cloudproject.services.services.UserService;
import it.unimi.cloudproject.utilities.AwsSdkUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.sns.SnsClient;

import java.util.function.Function;

@Configuration
public class ShopFunctionsConfiguration {
    @Autowired
    private UserService userService;

    @Autowired
    private ShopService shopService;

    @Bean
    public Function<InvocationWrapper<ShopCreationRequest>, ShopCreationResponse> createShop() {
        return (sc) -> {
            var shopCreation = new ShopCreation(sc.body().name(), sc.body().shopOwnerId(), sc.body().longitude(), sc.body().latitude());

//            var clientId = System.getProperty("aws.cognito.user_pool_client_id");
            var userPoolId = System.getProperty("aws.cognito.user_pool_id");

            final int shopId = this.shopService.addShop(shopCreation);
            try (var cognitoClient = CognitoIdentityProviderClient.create(); var snsClient = SnsClient.create()) {
                var userInfo = this.userService.getUser(sc.body().shopOwnerId());

                AwsSdkUtils.runSdkRequestAndAssertResult(
                        () -> cognitoClient.adminAddUserToGroup(addUserToGroupBuilder -> addUserToGroupBuilder
                                .userPoolId(userPoolId)
                                .groupName("shop-user-group")
                                .username(userInfo.username())),
                        (e) -> new CannotCreateShop(sc.body().shopOwnerId(), e));

                AwsSdkUtils.runSdkRequestAndAssertResult(
                        () -> snsClient.createTopic(b -> b.name(Integer.toString(shopId))),
                        (e) -> new CannotCreateShop(sc.body().shopOwnerId(), e));

                return new ShopCreationResponse(shopId);
            }
            catch (Exception exc) {
                this.shopService.deleteShop(sc.body().shopOwnerId(), shopId);
                throw exc;
            }
        };
    }
}
