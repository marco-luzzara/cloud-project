package it.unimi.cloudproject.application.factories;

import it.unimi.cloudproject.application.dto.UserCreationData;
import it.unimi.cloudproject.factories.bl.UserFactory;

public class UserDtoFactory {
    public static UserCreationData createUserCreationData() {
        var user = UserFactory.getUser();
        return new UserCreationData(user.username());
    }
}
