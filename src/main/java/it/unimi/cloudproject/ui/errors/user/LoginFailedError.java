package it.unimi.cloudproject.ui.errors.user;

import it.unimi.cloudproject.infrastructure.errors.Error;

public class LoginFailedError extends Error {
    public LoginFailedError(String email) {
        super("Login for user with email %s failed: the authentication service cannot process the request".formatted(email));
    }
}
