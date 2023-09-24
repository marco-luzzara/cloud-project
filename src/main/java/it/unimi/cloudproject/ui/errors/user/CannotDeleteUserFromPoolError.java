package it.unimi.cloudproject.ui.errors.user;

import it.unimi.cloudproject.infrastructure.errors.Error;

public class CannotDeleteUserFromPoolError extends Error {
    public CannotDeleteUserFromPoolError(int userId) {
        super("Cannot currently delete user %d".formatted(userId));
    }
}
