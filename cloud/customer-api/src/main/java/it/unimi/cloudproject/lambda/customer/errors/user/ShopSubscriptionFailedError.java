package it.unimi.cloudproject.lambda.customer.errors.user;

import it.unimi.cloudproject.infrastructure.errors.InternalException;

public class ShopSubscriptionFailedError extends InternalException {
    public ShopSubscriptionFailedError(int userId, int shopId, Throwable throwable) {
        super("User %d cannot subscribe to shop %d".formatted(userId, shopId), throwable);
    }
}
