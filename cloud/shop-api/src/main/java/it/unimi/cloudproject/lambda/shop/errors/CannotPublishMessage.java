package it.unimi.cloudproject.lambda.shop.errors;

import it.unimi.cloudproject.infrastructure.errors.InternalException;

public class CannotPublishMessage extends InternalException {
    public CannotPublishMessage(int shopId, Throwable throwable) {
        super("Cannot publish message for shop %d".formatted(shopId), throwable);
    }
}
