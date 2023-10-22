package it.unimi.cloudproject.lambda.authorizer.errors;

import it.unimi.cloudproject.infrastructure.errors.Error;

public class UnauthorizedUserForShopError extends Error {
    public UnauthorizedUserForShopError(int userId, int shopId) {
        super("User with id %d is not the owner of shop %d".formatted(userId, shopId));
    }
}
