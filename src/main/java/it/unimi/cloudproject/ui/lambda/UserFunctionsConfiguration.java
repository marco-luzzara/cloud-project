package it.unimi.cloudproject.ui.lambda;

import it.unimi.cloudproject.application.dto.UserCreationData;
import it.unimi.cloudproject.application.services.UserService;
import it.unimi.cloudproject.ui.dto.requests.user.UserCreationRequest;
import it.unimi.cloudproject.ui.dto.requests.user.UserDeletionRequest;
import it.unimi.cloudproject.ui.dto.requests.user.UserGetRequest;
import it.unimi.cloudproject.ui.dto.responses.user.UserCreationResponse;
import it.unimi.cloudproject.ui.dto.responses.user.UserGetResponse;
import it.unimi.cloudproject.ui.errors.user.InvalidUserIdError;
import it.unimi.cloudproject.ui.errors.user.RegistrationFailedError;
import it.unimi.cloudproject.ui.lambda.model.InvocationWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpResponse;

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

            Integer userId = null;
            try (var cognitoClient = CognitoIdentityProviderClient.builder().build()) {
                userId = this.userService.addUser(new UserCreationData(cr.body().username()));
                var signUpRequest = SignUpRequest.builder()
                        .clientId(clientId)
                        .username(cr.body().username())
                        .password(cr.body().password())
                        .userAttributes(AttributeType.builder().name("dbId").value(userId.toString()).build())
                        .build();

                var signUpResponse = cognitoClient.signUp(signUpRequest);
                if (!signUpResponse.userConfirmed())
                    throw new RegistrationFailedError(userId);
                return new UserCreationResponse(userId);
            }
            catch (Exception exc) {
                if (!Objects.isNull(userId))
                    this.userService.deleteUser(userId);
                throw exc;
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
