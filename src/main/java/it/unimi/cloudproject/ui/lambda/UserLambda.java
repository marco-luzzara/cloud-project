package it.unimi.cloudproject.ui.lambda;

import it.unimi.cloudproject.application.dto.UserCreationRequest;
import it.unimi.cloudproject.application.dto.responses.UserCreationResponse;
import it.unimi.cloudproject.application.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

@Configuration
public class UserLambda {
    @Autowired
    private UserService userService;

    @Bean
    public Function<UserCreationRequest, UserCreationResponse> createUser() {
        return this.userService::addUser;
    }
}