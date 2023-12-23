package it.unimi.cloudproject.lambda.admin.configurations;

import it.unimi.cloudproject.apigw.message.model.InvocationWrapper;
import it.unimi.cloudproject.lambda.admin.dto.requests.ShopCreationRequest;
import it.unimi.cloudproject.lambda.admin.dto.responses.ShopCreationResponse;
import it.unimi.cloudproject.lambda.admin.implementations.FunctionsImplementation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.function.context.MessageRoutingCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

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
