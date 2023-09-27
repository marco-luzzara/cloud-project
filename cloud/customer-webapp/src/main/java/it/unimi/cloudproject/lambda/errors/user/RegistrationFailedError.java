package it.unimi.cloudproject.lambda.errors.user;

import it.unimi.cloudproject.infrastructure.errors.Error;
import it.unimi.cloudproject.infrastructure.errors.InternalException;

public class RegistrationFailedError extends InternalException {
    public RegistrationFailedError(int userId, Throwable throwable) {
        super("Registration for user with id %d failed: the authentication service cannot process the request".formatted(userId), throwable);
    }
}
