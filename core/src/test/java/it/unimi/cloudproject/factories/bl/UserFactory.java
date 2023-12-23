package it.unimi.cloudproject.factories.bl;

import it.unimi.cloudproject.bl.User;

public class UserFactory {
    public static final String VALID_USERNAME = "test_user";
    public static final Integer VALID_ID = null;

    public static User getUser() {
        return new User(VALID_ID, VALID_USERNAME);
    }
}
