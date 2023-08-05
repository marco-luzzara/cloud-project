package it.unimi.cloudproject.application.factories;

import it.unimi.cloudproject.application.dto.requests.UserCreationRequest;
import it.unimi.cloudproject.factories.bl.UserFactory;

public class UserDtoFactory {
    public static UserCreationRequest createUserCreation() {
        var user = UserFactory.getUser();
        return new UserCreationRequest(user.username());
    }
}
