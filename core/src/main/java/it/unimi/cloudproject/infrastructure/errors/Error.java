package it.unimi.cloudproject.infrastructure.errors;

/**
 * Errors should be returned to the user, while exceptions should not
 */
public class Error extends RuntimeException {
    public Error(String message) {
        super(message);
    }
}
