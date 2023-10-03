package it.unimi.cloudproject.lambda.admin.configurations;

import it.unimi.cloudproject.apigw.message.model.InvocationWrapper;
import it.unimi.cloudproject.lambda.admin.dto.requests.shop.ShopCreationRequest;
import it.unimi.cloudproject.lambda.admin.dto.responses.shop.ShopCreationResponse;
import it.unimi.cloudproject.lambda.admin.errors.user.CannotPromoteUserToShopError;
import it.unimi.cloudproject.services.dto.ShopCreation;
import it.unimi.cloudproject.services.services.ShopService;
import it.unimi.cloudproject.services.services.UserService;
import it.unimi.cloudproject.utilities.AwsSdkUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.core.internal.http.loader.DefaultSdkHttpClientBuilder;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.utils.AttributeMap;

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
            try (var cognitoClient = CognitoIdentityProviderClient.builder()
                    .httpClient(new DefaultSdkHttpClientBuilder().buildWithDefaults(AttributeMap.builder()
                            .build()))
                    .build()) {
                var userInfo = this.userService.getUser(sc.body().shopOwnerId());

                AwsSdkUtils.runSdkRequestAndAssertResult(
                        () -> cognitoClient.adminAddUserToGroup(addUserToGroupBuilder -> addUserToGroupBuilder
                                .userPoolId(userPoolId)
                                .groupName("shop-user-group")
                                .username(userInfo.username())),
                        (e) -> new CannotPromoteUserToShopError(sc.body().shopOwnerId(), shopId, e));

                return new ShopCreationResponse(shopId);
            }
            catch (Exception exc) {
                this.shopService.deleteShop(shopId);
                throw exc;
            }
        };
    }
}