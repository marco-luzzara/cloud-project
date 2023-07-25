package it.unimi.cloudproject.application.errors;

import it.unimi.cloudproject.infrastructure.errors.Error;

public class InvalidShopIdError extends Error {
    public InvalidShopIdError(int shopId) {
        super("Shop with id %s does not exist".formatted(shopId));
    }
}
