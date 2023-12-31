package it.unimi.cloudproject.services.errors;

import it.unimi.cloudproject.infrastructure.errors.Error;

public class InvalidUsernameError extends Error {
    public InvalidUsernameError(int userId) {
        super("User with id %s does not exist".formatted(userId));
    }
}
