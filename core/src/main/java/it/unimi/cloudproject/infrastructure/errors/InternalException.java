package it.unimi.cloudproject.infrastructure.errors;

public class InternalException extends RuntimeException {
    public InternalException(String message, Throwable cause) {
        super(message, cause);
    }
}
