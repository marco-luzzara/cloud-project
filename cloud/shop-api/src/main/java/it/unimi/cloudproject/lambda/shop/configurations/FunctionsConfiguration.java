package it.unimi.cloudproject.lambda.shop.configurations;

import it.unimi.cloudproject.apigw.message.model.InvocationWrapper;
import it.unimi.cloudproject.lambda.shop.dto.requests.DeleteShopRequest;
import it.unimi.cloudproject.lambda.shop.dto.requests.PublishMessageRequest;
import it.unimi.cloudproject.lambda.shop.implementations.FunctionsImplementation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.function.context.MessageRoutingCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.Optional;
import java.util.function.Consumer;

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
    public Consumer<InvocationWrapper<DeleteShopRequest>> deleteShop() {
        return (ds) -> this.functionsImplementation.deleteShopImplWrapper(ds.body());
    }

    @Bean
    public Consumer<InvocationWrapper<PublishMessageRequest>> publishMessage() {
        return (pm) -> this.functionsImplementation.publishMessageImplWrapper(pm.body());
    }
}
