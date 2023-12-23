package it.unimi.cloudproject.lambda.customer.errors;

import it.unimi.cloudproject.infrastructure.errors.InternalException;

public class RegistrationFailedError extends InternalException {
    public RegistrationFailedError(int userId, Throwable throwable) {
        super("Registration for user with id %d failed: the authentication service cannot process the request".formatted(userId), throwable);
    }
}
