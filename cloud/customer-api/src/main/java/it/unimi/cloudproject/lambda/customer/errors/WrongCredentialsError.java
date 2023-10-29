package it.unimi.cloudproject.lambda.customer.errors;

import it.unimi.cloudproject.infrastructure.errors.Error;

public class WrongCredentialsError extends Error {
    public WrongCredentialsError(String email) {
        super("The password for the user %s is wrong".formatted(email));
    }
}
