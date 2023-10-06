package it.unimi.cloudproject.lambda.shop.configurations;

import it.unimi.cloudproject.apigw.message.model.InvocationWrapper;
import it.unimi.cloudproject.infrastructure.errors.InternalException;
import it.unimi.cloudproject.lambda.shop.dto.requests.DeleteShopRequest;
import it.unimi.cloudproject.lambda.shop.dto.requests.PublishMessageRequest;
import it.unimi.cloudproject.lambda.shop.errors.user.CannotPublishMessage;
import it.unimi.cloudproject.services.errors.UnauthorizedUserForShopError;
import it.unimi.cloudproject.services.services.ShopService;
import it.unimi.cloudproject.utilities.AwsSdkUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.sns.SnsClient;

import java.util.function.Consumer;
import java.util.function.Function;

@Configuration
public class ShopFunctionsConfiguration {
    @Autowired
    private ShopService shopService;

    @Bean
    public Consumer<InvocationWrapper<DeleteShopRequest>> deleteShop() {
        return (ds) -> this.shopService.deleteShop(ds.body().userId(), ds.body().shopId());
        // TODO: remove also the topic
    }

    @Bean
    public Consumer<InvocationWrapper<PublishMessageRequest>> publishMessage() {
        return (pm) -> {
            var shopInfo = this.shopService.findById(pm.body().shopId());
            // TODO: if custom authorizer is necessary, then I can move this check there
            if (shopInfo.shopOwnerId() != pm.body().userId())
                throw new UnauthorizedUserForShopError(pm.body().userId(), pm.body().shopId());
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
