package it.unimi.cloudproject.lambda.authorizer.errors;

import it.unimi.cloudproject.infrastructure.errors.InternalException;

public class CannotAuthorizeRequest extends InternalException {
    public CannotAuthorizeRequest(Throwable cause) {
        super("Cannot authorize request", cause);
    }
}
