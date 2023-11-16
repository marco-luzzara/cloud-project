package it.unimi.cloudproject.lambda.customer.implementations;

import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.instrumentation.annotations.AddingSpanAttributes;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import it.unimi.cloudproject.infrastructure.annotations.WithMeasuredExecutionTime;
import it.unimi.cloudproject.infrastructure.errors.InternalException;
import it.unimi.cloudproject.lambda.customer.dto.requests.user.*;
import it.unimi.cloudproject.lambda.customer.dto.responses.LoginResponse;
import it.unimi.cloudproject.lambda.customer.dto.responses.UserCreationResponse;
import it.unimi.cloudproject.lambda.customer.dto.responses.UserGetInfoResponse;
import it.unimi.cloudproject.lambda.customer.errors.*;
import it.unimi.cloudproject.infrastructure.monitoring.MetricsGenerator;
import it.unimi.cloudproject.services.dto.UserCreationData;
import it.unimi.cloudproject.services.services.ShopService;
import it.unimi.cloudproject.services.services.UserService;
import it.unimi.cloudproject.utilities.AwsSdkUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.internal.http.loader.DefaultSdkHttpClientBuilder;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.NotAuthorizedException;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.utils.AttributeMap;

import java.util.Map;
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

    // *************** User Login
    @WithSpan
    public LoginResponse loginUserImplWrapper(UserLoginRequest loginRequest) {
        return loginUserImpl(loginRequest, loginRequest.toString());
    }

    @WithMeasuredExecutionTime
    @AddingSpanAttributes
    private LoginResponse loginUserImpl(UserLoginRequest loginRequest, @SpanAttribute("input") String _requestInputDontUse) {
        var clientId = System.getProperty("aws.cognito.user_pool_client_id");
        var userPoolId = System.getProperty("aws.cognito.user_pool_id");

        try (var cognitoClient = CognitoIdentityProviderClient.builder()
                .httpClient(new DefaultSdkHttpClientBuilder().buildWithDefaults(AttributeMap.builder()
                        .build()))
                .build()) {

            var authResponse = AwsSdkUtils.runSdkRequestAndAssertResult(
                    () -> cognitoClient.initiateAuth(b -> b
                            .clientId(clientId)
                            .authFlow("USER_PASSWORD_AUTH")
                            .authParameters(
                                    Map.of(
                                            "USERNAME", loginRequest.username(),
                                            "PASSWORD", loginRequest.password()
                                    )
                            )),
                    (e) -> new LoginFailedError(loginRequest.username(), e));

            return new LoginResponse(
                    authResponse.authenticationResult().accessToken(),
                    authResponse.authenticationResult().idToken());
        }
        catch (NotAuthorizedException exc) {
            throw new WrongCredentialsError(loginRequest.username());
        }
    }

    // *************** Create User
    @WithSpan
    public UserCreationResponse createUserImplWrapper(UserCreationRequest cr) {
        return createUserImpl(cr, cr.toString());
    }

    @WithMeasuredExecutionTime
    @AddingSpanAttributes
    private UserCreationResponse createUserImpl(UserCreationRequest cr, @SpanAttribute("input") String _requestInputDontUse) {
        var clientId = System.getProperty("aws.cognito.user_pool_client_id");
        var userPoolId = System.getProperty("aws.cognito.user_pool_id");

        final int userId = this.userService.addUser(new UserCreationData(cr.username()));
        try (var cognitoClient = CognitoIdentityProviderClient.builder()
                .httpClient(new DefaultSdkHttpClientBuilder().buildWithDefaults(AttributeMap.builder()
                        .build()))
                .build()) {

            AwsSdkUtils.runSdkRequestAndAssertResult(
                    () -> cognitoClient.signUp(signUpBuilder -> signUpBuilder
                            .clientId(clientId)
                            .username(cr.username())
                            .password(cr.password())
                            .userAttributes(attrTypeBuilder -> attrTypeBuilder.name("custom:dbId").value(String.valueOf(userId)))),
                    (e) -> new RegistrationFailedError(userId, e));

            AwsSdkUtils.runSdkRequestAndAssertResult(
                    () -> cognitoClient.adminConfirmSignUp(b -> b
                            .username(cr.username())
                            .userPoolId(userPoolId)),
                    (e) -> new RegistrationFailedError(userId, e));

            AwsSdkUtils.runSdkRequestAndAssertResult(
                    () -> cognitoClient.adminAddUserToGroup(b -> b
                            .username(cr.username())
                            .userPoolId(userPoolId)
                            .groupName("customer-user-group")),
                    (e) -> new RegistrationFailedError(userId, e));

            return new UserCreationResponse(userId);
        } catch (Exception exc) {
            this.userService.deleteUser(userId);
            throw exc;
        }
    }

    // *************** Delete User
    @WithSpan
    public void deleteUserImplWrapper(UserDeletionRequest dr) {
        deleteUserImpl(dr, dr.toString());
    }

    @WithMeasuredExecutionTime
    @AddingSpanAttributes
    private void deleteUserImpl(UserDeletionRequest dr, @SpanAttribute("input") String _requestInputDontUse) {
        var userPoolId = System.getProperty("aws.cognito.user_pool_id");

        this.userService.deleteUser(dr.userId());
        try (var cognitoClient = CognitoIdentityProviderClient.builder()
                .httpClient(new DefaultSdkHttpClientBuilder().buildWithDefaults(AttributeMap.builder()
                        .build()))
                .build()) {
            // TODO: DeleteUser action is not available in the Localstack pro version, in the meantime I replace it with adminDeleteUser
            AwsSdkUtils.runSdkRequestAndAssertResult(() -> cognitoClient
                            .adminDeleteUser(b -> b.userPoolId(userPoolId).username(dr.username())),
                    (e) -> new CannotDeleteUserFromPoolError(dr.userId(), e));
        }
    }

    // *************** Get User
    @WithSpan
    public UserGetInfoResponse getUserImplWrapper(UserGetInfoRequest userGetRequest) {
        return getUserImpl(userGetRequest, userGetRequest.toString());
    }

    @WithMeasuredExecutionTime
    @AddingSpanAttributes
    private UserGetInfoResponse getUserImpl(UserGetInfoRequest userGetRequest, @SpanAttribute("input") String _requestInputDontUse) {
        var userInfo = this.userService.getUser(userGetRequest.userId());
        return new UserGetInfoResponse(userInfo.id(), userInfo.username());
    }

    // *************** User subscribe to Shop
    @WithSpan
    public void subscribeToShopImplWrapper(ShopSubscriptionRequest sr) {
        subscribeToShopImpl(sr, sr.toString());
    }

    @WithMeasuredExecutionTime
    @AddingSpanAttributes
    private void subscribeToShopImpl(ShopSubscriptionRequest sr, @SpanAttribute("input") String _requestInputDontUse) {
        shopService.findById(sr.shopId()); // throws if shop does not exist
        try (var snsClient = SnsClient.create()) {
            Function<Throwable, InternalException> exceptionFunction = (e) -> new ShopSubscriptionFailedError(sr.userId(), sr.shopId(), e);
            var topicArn = AwsSdkUtils.runSdkRequestAndAssertResult(() -> snsClient
                            .createTopic(b -> b.name(Integer.toString(sr.shopId()))),
                    exceptionFunction).topicArn();
            AwsSdkUtils.runSdkRequestAndAssertResult(() -> snsClient
                            .subscribe(b -> b.topicArn(topicArn)
                                    .protocol("email")
                                    .endpoint(sr.username())),
                    exceptionFunction);
        }
    }
}
