package it.unimi.cloudproject.ui.lambda;

import it.unimi.cloudproject.application.dto.UserCreationData;
import it.unimi.cloudproject.application.services.UserService;
import it.unimi.cloudproject.ui.dto.requests.user.UserCreationRequest;
import it.unimi.cloudproject.ui.dto.requests.user.UserDeletionRequest;
import it.unimi.cloudproject.ui.dto.requests.user.UserGetRequest;
import it.unimi.cloudproject.ui.dto.responses.user.UserCreationResponse;
import it.unimi.cloudproject.ui.dto.responses.user.UserGetResponse;
import it.unimi.cloudproject.ui.errors.user.InvalidUserIdError;
import it.unimi.cloudproject.ui.lambda.model.InvocationWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.function.Consumer;
import java.util.function.Function;

@Configuration
public class UserFunctionsConfiguration {
    @Autowired
    private UserService userService;

    @Bean
    public Function<InvocationWrapper<UserCreationRequest>, UserCreationResponse> createUser() {
        return (cr) -> {
            var id = this.userService.addUser(new UserCreationData(cr.body().username()));
            return new UserCreationResponse(id);
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
//    public Function<UserDeletionRequest> addShopToFavorite() {
//        return (dr) -> this.userService.addShopToFavorite(dr.id());
//    }
}
