package it.unimi.cloudproject.ui.errors.user;

import it.unimi.cloudproject.infrastructure.errors.Error;

public class RegistrationFailedError extends Error {
    public RegistrationFailedError(int userId) {
        super("Registration for user with id %d failed: the authentication service cannot process the request".formatted(userId));
    }
}
