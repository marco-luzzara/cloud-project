package it.unimi.cloudproject.lambda.shop.configurations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.function.context.MessageRoutingCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.Optional;

@Configuration
public class RoutingConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(RoutingConfiguration.class);

    @Bean
    public MessageRoutingCallback customRouter() {
        return new MessageRoutingCallback() {
            @Override
            public String routingResult(Message<?> message) {
//                var lambdaContext = (Context) message.getHeaders().get(AWSLambdaUtils.AWS_CONTEXT);
//                logger.info("Inspecting message headers - " + message.getHeaders());
                return Optional.ofNullable((String) message.getHeaders().get("X-Spring-Cloud-Function-Definition")).orElseThrow();
            }
        };
    }
}
