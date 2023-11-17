package it.unimi.cloudproject.lambda.admin.configurations;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import it.unimi.cloudproject.apigw.message.model.InvocationWrapper;
import it.unimi.cloudproject.lambda.admin.dto.requests.ShopCreationRequest;
import it.unimi.cloudproject.lambda.admin.dto.responses.ShopCreationResponse;
import it.unimi.cloudproject.lambda.admin.errors.CannotCreateShop;
import it.unimi.cloudproject.lambda.admin.implementations.FunctionsImplementation;
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
    public Function<InvocationWrapper<ShopCreationRequest>, ShopCreationResponse> createShop() {
        return (sc) -> this.functionsImplementation.createShopImplWrapper(sc.body());
    }
}
