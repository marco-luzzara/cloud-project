package it.unimi.cloudproject.lambda.configurations;

import it.unimi.cloudproject.apigw.message.model.InvocationWrapper;
import it.unimi.cloudproject.lambda.dto.requests.user.*;
import it.unimi.cloudproject.lambda.dto.responses.user.LoginResponse;
import it.unimi.cloudproject.lambda.dto.responses.user.UserCreationResponse;
import it.unimi.cloudproject.lambda.dto.responses.user.UserGetResponse;
import it.unimi.cloudproject.lambda.errors.user.CannotDeleteUserFromPoolError;
import it.unimi.cloudproject.lambda.errors.user.InvalidUserIdError;
import it.unimi.cloudproject.lambda.errors.user.LoginFailedError;
import it.unimi.cloudproject.lambda.errors.user.RegistrationFailedError;
import it.unimi.cloudproject.services.dto.UserCreationData;
import it.unimi.cloudproject.services.services.UserService;
import it.unimi.cloudproject.utilities.AwsSdkUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.core.internal.http.loader.DefaultSdkHttpClientBuilder;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.utils.AttributeMap;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@Configuration
public class UserFunctionsConfiguration {
    @Autowired
    private UserService userService;

    @Bean
    public Function<InvocationWrapper<UserCreationRequest>, UserCreationResponse> createUser() {
        return (cr) -> {
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
            }
            catch (Exception exc) {
                this.userService.deleteUser(userId);
                throw exc;
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
        };
    }

    @Bean
    public Consumer<InvocationWrapper<UserDeletionRequest>> deleteUser() {
        return (dr) -> {
            try (var cognitoClient = CognitoIdentityProviderClient.builder()
                    .httpClient(new DefaultSdkHttpClientBuilder().buildWithDefaults(AttributeMap.builder()
                            .build()))
                    .build()) {
                AwsSdkUtils.runSdkRequestAndAssertResult(() -> cognitoClient
                        .deleteUser(b -> b.accessToken(dr.headers().get("Authorization"))),
                        (e) -> new CannotDeleteUserFromPoolError(dr.body().userId(), e));
            }
            this.userService.deleteUser(dr.body().userId());
        };
    }

    @Bean
    public Function<InvocationWrapper<UserGetRequest>, UserGetResponse> getUser() {
        return userGetRequest -> this.userService.getUser(userGetRequest.body().userId())
                .map(ui -> new UserGetResponse(ui.id(), ui.username()))
                .orElseThrow(() -> new InvalidUserIdError(userGetRequest.body().userId()));
    }

    @Bean
    public Consumer<InvocationWrapper<ShopSubscriptionRequest>> addShopSubscription() {
        return (subscriptionRequest) -> this.userService.addShopToFavorite(subscriptionRequest.body().userId(), subscriptionRequest.body().shopId());
    }
}
