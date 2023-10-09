package it.unimi.cloudproject.helpers.factories;

import it.unimi.cloudproject.lambda.customer.dto.requests.user.UserCreationRequest;

public class UserDataFactory {
    private static int userCounter = 0;
    private static final String USER_PASSWORD = "testtest";
    private static final String ADMIN_PASSWORD = "adminadmin";

    public static UserCreationRequest getNewUser() {
        userCounter++;
        return new UserCreationRequest("testuser%d@amazon.com".formatted(userCounter), USER_PASSWORD);
    }

    public static UserCreationRequest getAdminUser() {
        return new UserCreationRequest("admin1@amazon.com", ADMIN_PASSWORD);
    }
}
