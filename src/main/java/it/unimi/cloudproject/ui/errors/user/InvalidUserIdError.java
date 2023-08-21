package it.unimi.cloudproject.ui.errors.user;

import it.unimi.cloudproject.infrastructure.errors.Error;

public class InvalidUserIdError extends Error {
    public InvalidUserIdError(int userId) {
        super("user with id %d does not exist".formatted(userId));
    }
}
