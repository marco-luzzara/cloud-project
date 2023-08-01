package it.unimi.cloudproject.ui.lambda.user;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import it.unimi.cloudproject.application.dto.UserCreation;
import it.unimi.cloudproject.application.services.UserService;
import it.unimi.cloudproject.ui.lambda.GenericLambda;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CreateUserLambda extends GenericLambda<UserCreation, Integer> {
    private final UserService userService;

    public CreateUserLambda(@Autowired UserService userService) {
        this.userService = userService;
    }

    @Override
    public Integer execute(UserCreation userCreation, LambdaLogger logger) {
        logger.log("Creating new user with params: " + userCreation);
        return this.userService.addUser(userCreation);
    }
}
