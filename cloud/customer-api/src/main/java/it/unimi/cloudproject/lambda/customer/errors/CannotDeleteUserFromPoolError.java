package it.unimi.cloudproject.lambda.customer.errors;

import it.unimi.cloudproject.infrastructure.errors.Error;
import it.unimi.cloudproject.infrastructure.errors.InternalException;

public class CannotDeleteUserFromPoolError extends InternalException {
    public CannotDeleteUserFromPoolError(int userId, Throwable throwable) {
        super("Cannot currently delete user %d".formatted(userId), throwable);
    }
}
