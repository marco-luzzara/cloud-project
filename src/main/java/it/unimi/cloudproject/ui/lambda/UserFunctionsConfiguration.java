package it.unimi.cloudproject.ui.lambda;

import it.unimi.cloudproject.application.dto.UserCreationData;
import it.unimi.cloudproject.application.services.UserService;
import it.unimi.cloudproject.infrastructure.utilities.AwsUtils;
import it.unimi.cloudproject.ui.dto.requests.user.*;
import it.unimi.cloudproject.ui.dto.responses.user.LoginResponse;
import it.unimi.cloudproject.ui.dto.responses.user.UserCreationResponse;
import it.unimi.cloudproject.ui.dto.responses.user.UserGetResponse;
import it.unimi.cloudproject.ui.errors.user.CannotDeleteUserFromPoolError;
import it.unimi.cloudproject.ui.errors.user.InvalidUserIdError;
import it.unimi.cloudproject.ui.errors.user.LoginFailedError;
import it.unimi.cloudproject.ui.errors.user.RegistrationFailedError;
import it.unimi.cloudproject.ui.lambda.model.InvocationWrapper;
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

                AwsUtils.runSdkRequestAndAssertResult(
                        () -> cognitoClient.signUp(signUpBuilder -> signUpBuilder
                                .clientId(clientId)
                                .username(cr.body().username())
                                .password(cr.body().password())
                                .userAttributes(attrTypeBuilder -> attrTypeBuilder.name("custom:dbId").value(String.valueOf(userId)))),
                        () -> new RegistrationFailedError(userId));

                AwsUtils.runSdkRequestAndAssertResult(
                        () -> cognitoClient.adminConfirmSignUp(b -> b
                                .username(cr.body().username())
                                .userPoolId(userPoolId)),
                        () -> new RegistrationFailedError(userId));

                AwsUtils.runSdkRequestAndAssertResult(
                        () -> cognitoClient.adminAddUserToGroup(b -> b
                                .username(cr.body().username())
                                .userPoolId(userPoolId)
                                .groupName("customer-user-group")),
                        () -> new RegistrationFailedError(userId));

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

                var authResponse = AwsUtils.runSdkRequestAndAssertResult(
                        () -> cognitoClient.initiateAuth(b -> b
                                .clientId(clientId)
                                .authFlow("USER_PASSWORD_AUTH")
                                .authParameters(
                                        Map.of(
                                                "USERNAME", loginRequest.body().username(),
                                                "PASSWORD", loginRequest.body().password()
                                        )
                                )),
                        () -> new LoginFailedError(loginRequest.body().username()));

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
                AwsUtils.runSdkRequestAndAssertResult(() -> cognitoClient
                        .deleteUser(b -> b.accessToken(dr.headers().get("Authorization"))),
                        () -> new CannotDeleteUserFromPoolError(dr.body().userId()));
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
