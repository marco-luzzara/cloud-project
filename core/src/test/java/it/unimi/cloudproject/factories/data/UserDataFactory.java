package it.unimi.cloudproject.factories.data;

import it.unimi.cloudproject.data.model.UserData;
import it.unimi.cloudproject.data.repositories.UserRepository;
import it.unimi.cloudproject.factories.bl.UserFactory;

public class UserDataFactory {
    public static UserData createUser(UserRepository userRepo) {
        var user = UserFactory.getUser();
        var userData = UserData.fromUser(user);
        return userRepo.save(userData);
    }
}
