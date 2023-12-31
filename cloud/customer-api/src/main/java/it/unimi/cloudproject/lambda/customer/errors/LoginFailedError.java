package it.unimi.cloudproject.lambda.customer.errors;

import it.unimi.cloudproject.infrastructure.errors.InternalException;

public class LoginFailedError extends InternalException {
    public LoginFailedError(String email, Throwable throwable) {
        super("Login for user with email %s failed: the authentication service cannot process the request".formatted(email), throwable);
    }
}
