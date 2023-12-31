package it.unimi.cloudproject.factories.services;

import it.unimi.cloudproject.services.dto.UserCreationData;
import it.unimi.cloudproject.factories.bl.UserFactory;

public class UserDtoFactory {
    public static UserCreationData createUserCreationData() {
        var user = UserFactory.getUser();
        return new UserCreationData(user.username());
    }
}
