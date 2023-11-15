package it.unimi.cloudproject.lambda.customer.configurations;

import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.instrumentation.annotations.AddingSpanAttributes;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import it.unimi.cloudproject.apigw.message.model.InvocationWrapper;
import it.unimi.cloudproject.infrastructure.annotations.WithMeasuredExecutionTime;
import it.unimi.cloudproject.infrastructure.errors.InternalException;
import it.unimi.cloudproject.lambda.customer.dto.requests.user.*;
import it.unimi.cloudproject.lambda.customer.dto.responses.LoginResponse;
import it.unimi.cloudproject.lambda.customer.dto.responses.UserCreationResponse;
import it.unimi.cloudproject.lambda.customer.dto.responses.UserGetInfoResponse;
import it.unimi.cloudproject.lambda.customer.errors.*;
import it.unimi.cloudproject.services.dto.UserCreationData;
import it.unimi.cloudproject.services.services.ShopService;
import it.unimi.cloudproject.services.services.UserService;
import it.unimi.cloudproject.utilities.AwsSdkUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.function.context.MessageRoutingCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import software.amazon.awssdk.core.internal.http.loader.DefaultSdkHttpClientBuilder;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.NotAuthorizedException;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.utils.AttributeMap;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@Configuration
public class FunctionsConfiguration {
    @Autowired
    private UserService userService;

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

    private ShopService shopService;

    @Bean
    public Function<InvocationWrapper<UserCreationRequest>, UserCreationResponse> createUser() {
        return (cr) -> createUserImpl(cr.body());
    }

    @WithSpan
    private UserCreationResponse createUserImpl(UserCreationRequest cr) {
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

    @Bean
    public Function<InvocationWrapper<UserLoginRequest>, LoginResponse> loginUser() {
        return (loginRequest) -> loginUserImplWrapper(loginRequest.body());
    }

    @WithSpan
    private LoginResponse loginUserImplWrapper(UserLoginRequest loginRequest) {
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

    @Bean
    public Consumer<InvocationWrapper<UserDeletionRequest>> deleteUser() {
        return (dr) -> deleteUserImpl(dr.body());
    }

    @WithSpan
    private void deleteUserImpl(UserDeletionRequest dr) {
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

    @Bean
    public Function<InvocationWrapper<UserGetInfoRequest>, UserGetInfoResponse> getUser() {
        return userGetRequest -> getUserImpl(userGetRequest.body());
    }

    @WithSpan
    private UserGetInfoResponse getUserImpl(UserGetInfoRequest userGetRequest) {
        var userInfo = this.userService.getUser(userGetRequest.userId());
        return new UserGetInfoResponse(userInfo.id(), userInfo.username());
    }

    @Bean
    public Consumer<InvocationWrapper<ShopSubscriptionRequest>> subscribeToShop() {
        return (sr) -> {
            subscribeToShopImpl(sr.body());
        };
    }

    @WithSpan
    private void subscribeToShopImpl(ShopSubscriptionRequest sr) {
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
