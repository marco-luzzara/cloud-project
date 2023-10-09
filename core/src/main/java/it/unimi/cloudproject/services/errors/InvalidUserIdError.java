package it.unimi.cloudproject.services.errors;

import it.unimi.cloudproject.infrastructure.errors.Error;

public class InvalidUserIdError extends Error {
    public InvalidUserIdError(int userId) {
        super("User with id %d does not exist".formatted(userId));
    }
}
