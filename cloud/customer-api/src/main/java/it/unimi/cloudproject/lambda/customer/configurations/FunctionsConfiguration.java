package it.unimi.cloudproject.lambda.customer.configurations;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import it.unimi.cloudproject.CustomerApi;
import it.unimi.cloudproject.apigw.message.model.InvocationWrapper;
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
    private final Tracer tracer;
    private final OpenTelemetry openTelemetry;

    @Autowired
    private UserService userService;

    @Autowired
    FunctionsConfiguration(OpenTelemetry openTelemetry) {
        this.openTelemetry = openTelemetry;
        this.tracer = openTelemetry.getTracer(CustomerApi.class.getName(), "1.0.0");
    }

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

    private ShopService shopService;

    @Bean
    public Function<InvocationWrapper<UserCreationRequest>, UserCreationResponse> createUser() {
        return (cr) -> {
            var span = tracer.spanBuilder("createUser").startSpan();

            // Make the span the current span
            try (var scope = span.makeCurrent()) {
                var clientId = System.getProperty("aws.cognito.user_pool_client_id");
                var userPoolId = System.getProperty("aws.cognito.user_pool_id");

                final int userId = this.userService.addUser(new UserCreationData(cr.body().username()));
                try (var cognitoClient = CognitoIdentityProviderClient.builder()
                        .httpClient(new DefaultSdkHttpClientBuilder().buildWithDefaults(AttributeMap.builder()
                                .build()))
                        .build()) {

                    AwsSdkUtils.runSdkRequestAndAssertResult(
                            () -> cognitoClient.signUp(signUpBuilder -> signUpBuilder
                                    .clientId(clientId)
                                    .username(cr.body().username())
                                    .password(cr.body().password())
                                    .userAttributes(attrTypeBuilder -> attrTypeBuilder.name("custom:dbId").value(String.valueOf(userId)))),
                            (e) -> new RegistrationFailedError(userId, e));

                    AwsSdkUtils.runSdkRequestAndAssertResult(
                            () -> cognitoClient.adminConfirmSignUp(b -> b
                                    .username(cr.body().username())
                                    .userPoolId(userPoolId)),
                            (e) -> new RegistrationFailedError(userId, e));

                    AwsSdkUtils.runSdkRequestAndAssertResult(
                            () -> cognitoClient.adminAddUserToGroup(b -> b
                                    .username(cr.body().username())
                                    .userPoolId(userPoolId)
                                    .groupName("customer-user-group")),
                            (e) -> new RegistrationFailedError(userId, e));

                    return new UserCreationResponse(userId);
                } catch (Exception exc) {
                    this.userService.deleteUser(userId);
                    throw exc;
                }
            }
            catch(Throwable t) {
                span.recordException(t);
                throw t;
            } finally {
                span.end();
            }
        };
    }

    @Bean
    public Function<InvocationWrapper<UserLoginRequest>, LoginResponse> loginUser() {
        return (loginRequest) -> {
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
                                                "USERNAME", loginRequest.body().username(),
                                                "PASSWORD", loginRequest.body().password()
                                        )
                                )),
                        (e) -> new LoginFailedError(loginRequest.body().username(), e));

                return new LoginResponse(
                        authResponse.authenticationResult().accessToken(),
                        authResponse.authenticationResult().idToken());
            }
            catch (NotAuthorizedException exc) {
                throw new WrongCredentialsError(loginRequest.body().username());
            }
        };
    }

    @Bean
    public Consumer<InvocationWrapper<UserDeletionRequest>> deleteUser() {
        return (dr) -> {
            var userPoolId = System.getProperty("aws.cognito.user_pool_id");

            this.userService.deleteUser(dr.body().userId());
            try (var cognitoClient = CognitoIdentityProviderClient.builder()
                    .httpClient(new DefaultSdkHttpClientBuilder().buildWithDefaults(AttributeMap.builder()
                            .build()))
                    .build()) {
                // TODO: DeleteUser action is not available in the Localstack pro version, in the meantime I replace it with adminDeleteUser
                AwsSdkUtils.runSdkRequestAndAssertResult(() -> cognitoClient
                        .adminDeleteUser(b -> b.userPoolId(userPoolId).username(dr.body().username())),
                        (e) -> new CannotDeleteUserFromPoolError(dr.body().userId(), e));
            }
        };
    }

    @Bean
    public Function<InvocationWrapper<UserGetInfoRequest>, UserGetInfoResponse> getUser() {
        return userGetRequest -> {
            var userInfo = this.userService.getUser(userGetRequest.body().userId());
            return new UserGetInfoResponse(userInfo.id(), userInfo.username());
        };
    }

    @Bean
    public Consumer<InvocationWrapper<ShopSubscriptionRequest>> subscribeToShop() {
        return (sr) -> {
            shopService.findById(sr.body().shopId()); // throws if shop does not exist
            try (var snsClient = SnsClient.create()) {
                Function<Throwable, InternalException> exceptionFunction = (e) -> new ShopSubscriptionFailedError(sr.body().userId(), sr.body().shopId(), e);
                var topicArn = AwsSdkUtils.runSdkRequestAndAssertResult(() -> snsClient
                        .createTopic(b -> b.name(Integer.toString(sr.body().shopId()))),
                        exceptionFunction).topicArn();
                AwsSdkUtils.runSdkRequestAndAssertResult(() -> snsClient
                                .subscribe(b -> b.topicArn(topicArn)
                                        .protocol("email")
                                        .endpoint(sr.body().username())),
                        exceptionFunction);
            }
        };
    }
}
