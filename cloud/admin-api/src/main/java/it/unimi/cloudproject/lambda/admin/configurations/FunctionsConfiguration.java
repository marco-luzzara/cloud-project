package it.unimi.cloudproject.lambda.admin.configurations;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import it.unimi.cloudproject.apigw.message.model.InvocationWrapper;
import it.unimi.cloudproject.lambda.admin.dto.requests.ShopCreationRequest;
import it.unimi.cloudproject.lambda.admin.dto.responses.ShopCreationResponse;
import it.unimi.cloudproject.lambda.admin.errors.CannotCreateShop;
import it.unimi.cloudproject.services.dto.ShopCreation;
import it.unimi.cloudproject.services.services.ShopService;
import it.unimi.cloudproject.services.services.UserService;
import it.unimi.cloudproject.utilities.AwsSdkUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.function.context.MessageRoutingCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.sns.SnsClient;

import java.util.Optional;
import java.util.function.Function;

@Configuration
public class FunctionsConfiguration {
    @Autowired
    private UserService userService;

    @Autowired
    private ShopService shopService;

    @Bean
    public MessageRoutingCallback customRouter() {
        return new MessageRoutingCallback() {
            @Override
            public String routingResult(Message<?> message) {
//                var lambdaContext = (Context) message.getHeaders().get(AWSLambdaUtils.AWS_CONTEXT);
                return Optional.ofNullable((String) message.getHeaders().get("X-Spring-Cloud-Function-Definition")).orElseThrow();
            }
        };
    }

    @Bean
    public Function<InvocationWrapper<ShopCreationRequest>, ShopCreationResponse> createShop() {
        return (sc) -> createShopImpl(sc.body());
    }

    @WithSpan
    private ShopCreationResponse createShopImpl(ShopCreationRequest sc) {
        var shopCreation = new ShopCreation(sc.name(), sc.shopOwnerId(), sc.longitude(), sc.latitude());

//            var clientId = System.getProperty("aws.cognito.user_pool_client_id");
        var userPoolId = System.getProperty("aws.cognito.user_pool_id");

        final int shopId = this.shopService.addShop(shopCreation);
        try (var cognitoClient = CognitoIdentityProviderClient.create(); var snsClient = SnsClient.create()) {
            var userInfo = this.userService.getUser(sc.shopOwnerId());

            AwsSdkUtils.runSdkRequestAndAssertResult(
                    () -> cognitoClient.adminAddUserToGroup(addUserToGroupBuilder -> addUserToGroupBuilder
                            .userPoolId(userPoolId)
                            .groupName("shop-user-group")
                            .username(userInfo.username())),
                    (e) -> new CannotCreateShop(sc.shopOwnerId(), e));

            AwsSdkUtils.runSdkRequestAndAssertResult(
                    () -> snsClient.createTopic(b -> b.name(Integer.toString(shopId))),
                    (e) -> new CannotCreateShop(sc.shopOwnerId(), e));

            return new ShopCreationResponse(shopId);
        }
        catch (Exception exc) {
            this.shopService.deleteShop(shopId);
            throw exc;
        }
    }
}
