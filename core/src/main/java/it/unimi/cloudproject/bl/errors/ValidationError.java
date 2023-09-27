package it.unimi.cloudproject.bl.errors;

import it.unimi.cloudproject.bl.Shop;
import it.unimi.cloudproject.bl.User;
import it.unimi.cloudproject.infrastructure.errors.Error;

public class ValidationError {
    public static class DuplicateShopForUserError extends Error {
        public DuplicateShopForUserError(User user, Shop duplicateShop) {
            super("User %s tried to add the shop %s (%f, %f) to his favorites".formatted(
                    user.username(), duplicateShop.name(),
                    duplicateShop.coordinates().longitude(), duplicateShop.coordinates().latitude()));
        }
    }
    public static class EmptyNameForUserError extends Error {
        public EmptyNameForUserError() {
            super("A user cannot have an empty name");
        }
    }

    public static class EmptyNameForShopError extends Error {
        public EmptyNameForShopError() {
            super("A shop cannot have an empty name");
        }
    }
}
