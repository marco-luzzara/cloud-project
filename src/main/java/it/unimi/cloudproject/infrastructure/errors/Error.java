package it.unimi.cloudproject.infrastructure.errors;

/**
 * Errors should be returned to the user, while exceptions should not
 */
public class Error extends RuntimeException {
    public Error() {
    }

    public Error(String message) {
        super(message);
    }

    public Error(String message, Throwable cause) {
        super(message, cause);
    }

    public Error(Throwable cause) {
        super(cause);
    }

    public Error(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
