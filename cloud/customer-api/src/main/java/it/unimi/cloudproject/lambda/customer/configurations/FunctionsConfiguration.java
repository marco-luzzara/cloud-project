package it.unimi.cloudproject.lambda.customer.configurations;

import it.unimi.cloudproject.apigw.message.model.InvocationWrapper;
import it.unimi.cloudproject.lambda.customer.dto.requests.user.*;
import it.unimi.cloudproject.lambda.customer.dto.responses.LoginResponse;
import it.unimi.cloudproject.lambda.customer.dto.responses.UserCreationResponse;
import it.unimi.cloudproject.lambda.customer.dto.responses.UserGetInfoResponse;
import it.unimi.cloudproject.lambda.customer.implementations.FunctionsImplementation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.function.context.MessageRoutingCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@Configuration
public class FunctionsConfiguration {
    @Autowired
    private FunctionsImplementation functionsImplementation;

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
    public Function<InvocationWrapper<UserCreationRequest>, UserCreationResponse> createUser() {
        return (cr) -> this.functionsImplementation.createUserImplWrapper(cr.body());
    }

    @Bean
    public Function<InvocationWrapper<UserLoginRequest>, LoginResponse> loginUser() {
        return (loginRequest) -> this.functionsImplementation.loginUserImplWrapper(loginRequest.body());
    }

    @Bean
    public Consumer<InvocationWrapper<UserDeletionRequest>> deleteUser() {
        return (dr) -> this.functionsImplementation.deleteUserImplWrapper(dr.body());
    }

    @Bean
    public Function<InvocationWrapper<UserGetInfoRequest>, UserGetInfoResponse> getUser() {
        return userGetRequest -> this.functionsImplementation.getUserImplWrapper(userGetRequest.body());
    }

    @Bean
    public Consumer<InvocationWrapper<ShopSubscriptionRequest>> subscribeToShop() {
        return (sr) -> this.functionsImplementation.subscribeToShopImplWrapper(sr.body());
    }
}
