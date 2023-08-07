package it.unimi.cloudproject.ui.lambda;

import it.unimi.cloudproject.application.dto.UserCreationData;
import it.unimi.cloudproject.application.dto.UserInfo;
import it.unimi.cloudproject.ui.dto.requests.UserGetRequest;
import it.unimi.cloudproject.ui.dto.requests.UserCreationRequest;
import it.unimi.cloudproject.ui.dto.requests.UserDeletionRequest;
import it.unimi.cloudproject.ui.dto.responses.UserCreationResponse;
import it.unimi.cloudproject.application.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;
import java.util.function.Function;

@Configuration
public class UserLambda {
    @Autowired
    private UserService userService;

    @Bean
    public Function<UserCreationRequest, UserCreationResponse> createUser() {
        return (cr) -> {
            var id = this.userService.addUser(new UserCreationData(cr.username()));
            return new UserCreationResponse(id);
        };
    }

    @Bean
    public Consumer<UserDeletionRequest> deleteUser() {
        return (dr) -> this.userService.deleteUser(dr.id());
    }

    @Bean
    public Function<UserGetRequest, UserInfo> getUser() {
        return userGetRequest -> this.userService.getUser(userGetRequest.username()).orElseThrow();
    }
}
