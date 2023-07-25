package it.unimi.cloudproject.application.errors;

import it.unimi.cloudproject.infrastructure.errors.Error;

public class InvalidUsernameError extends Error {
    public InvalidUsernameError(String username) {
        super("User with username %s does not exist".formatted(username));
    }
}
