package it.unimi.cloudproject.lambda.admin.errors;

import it.unimi.cloudproject.infrastructure.errors.InternalException;

public class CannotCreateShop extends InternalException {
    public CannotCreateShop(int userId, Throwable throwable) {
        super("Cannot currently create shop for user %d".formatted(userId), throwable);
    }
}
