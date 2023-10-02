package it.unimi.cloudproject.lambda.admin.errors.user;

import it.unimi.cloudproject.infrastructure.errors.InternalException;

public class CannotPromoteUserToShopError extends InternalException {
    public CannotPromoteUserToShopError(int userId, int shopId, Throwable throwable) {
        super("Cannot currently promote user account (id: %d) to shop account for shop %d".formatted(userId, shopId), throwable);
    }
}
