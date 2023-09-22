package it.unimi.cloudproject.ui.lambda;

import it.unimi.cloudproject.application.dto.UserCreationData;
import it.unimi.cloudproject.application.services.UserService;
import it.unimi.cloudproject.infrastructure.utilities.AwsUtils;
import it.unimi.cloudproject.ui.dto.requests.user.UserCreationRequest;
import it.unimi.cloudproject.ui.dto.requests.user.UserDeletionRequest;
import it.unimi.cloudproject.ui.dto.requests.user.UserGetRequest;
import it.unimi.cloudproject.ui.dto.requests.user.UserLoginRequest;
import it.unimi.cloudproject.ui.dto.responses.user.LoginResponse;
import it.unimi.cloudproject.ui.dto.responses.user.UserCreationResponse;
import it.unimi.cloudproject.ui.dto.responses.user.UserGetResponse;
import it.unimi.cloudproject.ui.errors.user.InvalidUserIdError;
import it.unimi.cloudproject.ui.errors.user.LoginFailedError;
import it.unimi.cloudproject.ui.errors.user.RegistrationFailedError;
import it.unimi.cloudproject.ui.lambda.model.InvocationWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.core.internal.http.loader.DefaultSdkHttpClientBuilder;
import software.amazon.awssdk.http.SdkHttpConfigurationOption;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminConfirmSignUpRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpRequest;
import software.amazon.awssdk.utils.AttributeMap;

import java.util.Map;
import java.util.Objects;
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
                            .put(SdkHttpConfigurationOption.TRUST_ALL_CERTIFICATES, true)
                            .build()))
                    .build()) {

                AwsUtils.runSdkRequestAndAssertResult(
                        () -> cognitoClient.signUp(signUpBuilder -> signUpBuilder
                                .clientId(clientId)
                                .username(cr.body().username())
                                .password(cr.body().password())
                                .userAttributes(attrTypeBuilder -> attrTypeBuilder.name("dbId").value(String.valueOf(userId)))),
                        () -> new RegistrationFailedError(userId));

                AwsUtils.runSdkRequestAndAssertResult(
                        () -> cognitoClient.adminConfirmSignUp(b -> b
                                .username(cr.body().username())
                                .userPoolId(userPoolId)),
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
                            .put(SdkHttpConfigurationOption.TRUST_ALL_CERTIFICATES, true)
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

                return new LoginResponse(authResponse.authenticationResult().accessToken());
            }
        };
    }

    @Bean
    public Consumer<InvocationWrapper<UserDeletionRequest>> deleteUser() {
        return (dr) -> this.userService.deleteUser(dr.body().id());
    }

    @Bean
    public Function<InvocationWrapper<UserGetRequest>, UserGetResponse> getUser() {
        return userGetRequest -> this.userService.getUser(userGetRequest.body().id())
                .map(ui -> new UserGetResponse(ui.id(), ui.username()))
                .orElseThrow(() -> new InvalidUserIdError(userGetRequest.body().id()));
    }

//    @Bean
//    public Consumer<InvocationWrapper<>> addShopToFavorite() {
//        return (dr) -> this.userService.addShopToFavorite(dr.id());
//    }
}
