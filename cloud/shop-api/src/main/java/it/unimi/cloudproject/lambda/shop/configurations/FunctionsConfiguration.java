package it.unimi.cloudproject.lambda.shop.configurations;

import it.unimi.cloudproject.apigw.message.model.InvocationWrapper;
import it.unimi.cloudproject.infrastructure.errors.InternalException;
import it.unimi.cloudproject.lambda.shop.dto.requests.DeleteShopRequest;
import it.unimi.cloudproject.lambda.shop.dto.requests.PublishMessageRequest;
import it.unimi.cloudproject.lambda.shop.errors.CannotPublishMessage;
import it.unimi.cloudproject.services.services.ShopService;
import it.unimi.cloudproject.utilities.AwsSdkUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.function.context.MessageRoutingCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import software.amazon.awssdk.services.sns.SnsClient;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@Configuration
public class FunctionsConfiguration {
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
    public Consumer<InvocationWrapper<DeleteShopRequest>> deleteShop() {
        return (ds) -> {
            this.shopService.deleteShop(ds.body().shopId());
            try (var snsClient = SnsClient.create()) {
                Function<Throwable, InternalException> exceptionFunction = (e) -> new CannotPublishMessage(ds.body().shopId(), e);
                var topicArn = AwsSdkUtils.runSdkRequestAndAssertResult(() -> snsClient
                                .createTopic(b -> b.name(Integer.toString(ds.body().shopId()))),
                        exceptionFunction).topicArn();
                AwsSdkUtils.runSdkRequestAndAssertResult(
                        () -> snsClient.deleteTopic(b -> b.topicArn(topicArn)),
                        exceptionFunction);
            }
        };
    }

    @Bean
    public Consumer<InvocationWrapper<PublishMessageRequest>> publishMessage() {
        return (pm) -> {
            this.shopService.findById(pm.body().shopId());
            try (var snsClient = SnsClient.create()) {
                Function<Throwable, InternalException> exceptionFunction = (e) -> new CannotPublishMessage(pm.body().shopId(), e);
                var topicArn = AwsSdkUtils.runSdkRequestAndAssertResult(() -> snsClient
                                .createTopic(b -> b.name(Integer.toString(pm.body().shopId()))),
                        exceptionFunction).topicArn();
                AwsSdkUtils.runSdkRequestAndAssertResult(
                        () -> snsClient.publish(b -> b
                                .message(pm.body().message())
                                .topicArn(topicArn)),
                        exceptionFunction);
            }
        };
    }
}
