package it.unimi.cloudproject.ui.lambda.user;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import it.unimi.cloudproject.application.dto.UserCreation;
import it.unimi.cloudproject.application.services.UserService;
import it.unimi.cloudproject.ui.lambda.GenericLambda;
import org.springframework.beans.factory.annotation.Autowired;

public class AddUserLambda extends GenericLambda<UserCreation, Integer> {
    @Autowired
    private UserService userService;

    @Override
    public Integer execute(UserCreation userCreation, LambdaLogger logger) {
        return userService.addUser(userCreation);
    }
}
