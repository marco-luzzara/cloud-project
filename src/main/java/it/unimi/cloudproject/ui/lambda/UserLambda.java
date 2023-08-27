package it.unimi.cloudproject.ui.lambda;

import it.unimi.cloudproject.application.dto.UserCreationData;
import it.unimi.cloudproject.application.services.UserService;
import it.unimi.cloudproject.ui.dto.requests.user.UserCreationRequest;
import it.unimi.cloudproject.ui.dto.requests.user.UserDeletionRequest;
import it.unimi.cloudproject.ui.dto.requests.user.UserGetRequest;
import it.unimi.cloudproject.ui.dto.responses.user.UserCreationResponse;
import it.unimi.cloudproject.ui.dto.responses.user.UserGetResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

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
    public Function<Message<UserGetRequest>, Message<UserGetResponse>> getUser() {
        return userGetRequestMessage -> {
            var userGetRequest = userGetRequestMessage.getPayload();

            return this.userService.getUser(userGetRequest.id())
                    .map(userInfo -> MessageBuilder
                            .withPayload(new UserGetResponse(userInfo.id(), userInfo.username()))
                            .copyHeaders(userGetRequestMessage.getHeaders())
                            .setHeader("statusCode", 200)
                            .build())
                    .orElse(MessageBuilder
                            .withPayload(new UserGetResponse(-1, "Invalid id"))
                            .copyHeaders(userGetRequestMessage.getHeaders())
                            .setHeader("statusCode", 404)
                            .build());

        };
    }

//    @Bean
//    public Function<UserDeletionRequest> addShopToFavorite() {
//        return (dr) -> this.userService.addShopToFavorite(dr.id());
//    }
}
