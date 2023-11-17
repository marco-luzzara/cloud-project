package it.unimi.cloudproject.lambda.shop.implementations;

import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.instrumentation.annotations.AddingSpanAttributes;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import it.unimi.cloudproject.infrastructure.annotations.WithMeasuredExecutionTime;
import it.unimi.cloudproject.infrastructure.errors.InternalException;
import it.unimi.cloudproject.infrastructure.monitoring.MetricsGenerator;
import it.unimi.cloudproject.lambda.shop.dto.requests.DeleteShopRequest;
import it.unimi.cloudproject.lambda.shop.dto.requests.PublishMessageRequest;
import it.unimi.cloudproject.lambda.shop.errors.CannotPublishMessage;
import it.unimi.cloudproject.services.services.ShopService;
import it.unimi.cloudproject.services.services.UserService;
import it.unimi.cloudproject.utilities.AwsSdkUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;

import java.util.function.Function;

@Component
public class FunctionsImplementation implements MetricsGenerator {

    @Autowired
    private UserService userService;

    @Autowired
    private ShopService shopService;

    @Autowired
    private Meter meter;

    @Override
    public Meter getMeter() {
        return this.meter;
    }

    // *************** Delete Shop
    @WithSpan
    public void deleteShopImplWrapper(DeleteShopRequest ds) {
        deleteShopImpl(ds, ds.toString());
    }

    @WithMeasuredExecutionTime
    @AddingSpanAttributes
    private void deleteShopImpl(DeleteShopRequest ds, @SpanAttribute("input") String _requestInputDontUse) {
        this.shopService.deleteShop(ds.shopId());
        try (var snsClient = SnsClient.create()) {
            Function<Throwable, InternalException> exceptionFunction = (e) -> new CannotPublishMessage(ds.shopId(), e);
            var topicArn = AwsSdkUtils.runSdkRequestAndAssertResult(() -> snsClient
                            .createTopic(b -> b.name(Integer.toString(ds.shopId()))),
                    exceptionFunction).topicArn();
            AwsSdkUtils.runSdkRequestAndAssertResult(
                    () -> snsClient.deleteTopic(b -> b.topicArn(topicArn)),
                    exceptionFunction);
        }
    }

    // *************** Publish Message
    @WithSpan
    public void publishMessageImplWrapper(PublishMessageRequest pm) {
        publishMessageImpl(pm, pm.toString());
    }

    @WithMeasuredExecutionTime
    @AddingSpanAttributes
    private void publishMessageImpl(PublishMessageRequest pm, @SpanAttribute("input") String _requestInputDontUse) {
        this.shopService.findById(pm.shopId());
        try (var snsClient = SnsClient.create()) {
            Function<Throwable, InternalException> exceptionFunction = (e) -> new CannotPublishMessage(pm.shopId(), e);
            var topicArn = AwsSdkUtils.runSdkRequestAndAssertResult(() -> snsClient
                            .createTopic(b -> b.name(Integer.toString(pm.shopId()))),
                    exceptionFunction).topicArn();
            AwsSdkUtils.runSdkRequestAndAssertResult(
                    () -> snsClient.publish(b -> b
                            .message(pm.message())
                            .topicArn(topicArn)),
                    exceptionFunction);
        }
    }
}
