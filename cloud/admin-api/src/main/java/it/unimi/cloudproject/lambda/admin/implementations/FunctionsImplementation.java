package it.unimi.cloudproject.lambda.admin.implementations;

import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.instrumentation.annotations.AddingSpanAttributes;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import it.unimi.cloudproject.infrastructure.annotations.WithMeasuredExecutionTime;
import it.unimi.cloudproject.infrastructure.monitoring.MetricsGenerator;
import it.unimi.cloudproject.lambda.admin.dto.requests.ShopCreationRequest;
import it.unimi.cloudproject.lambda.admin.dto.responses.ShopCreationResponse;
import it.unimi.cloudproject.lambda.admin.errors.CannotCreateShop;
import it.unimi.cloudproject.services.dto.ShopCreation;
import it.unimi.cloudproject.services.services.ShopService;
import it.unimi.cloudproject.services.services.UserService;
import it.unimi.cloudproject.utilities.AwsSdkUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.sns.SnsClient;

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

    // *************** Create Shop
    @WithSpan
    public ShopCreationResponse createShopImplWrapper(ShopCreationRequest sc) {
        return createShopImpl(sc, sc.toString());
    }

    @WithMeasuredExecutionTime
    @AddingSpanAttributes
    private ShopCreationResponse createShopImpl(ShopCreationRequest sc, @SpanAttribute("input") String _requestInputDontUse) {
        var shopCreation = new ShopCreation(sc.name(), sc.shopOwnerId(), sc.longitude(), sc.latitude());

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
        } catch (Exception exc) {
            this.shopService.deleteShop(shopId);
            throw exc;
        }
    }
}
