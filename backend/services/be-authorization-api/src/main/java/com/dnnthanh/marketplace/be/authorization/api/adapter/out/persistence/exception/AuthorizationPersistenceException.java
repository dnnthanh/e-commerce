package com.dnnthanh.marketplace.be.authorization.api.adapter.out.persistence.exception;

/** Infrastructure failure while reading or serializing durable authorization state. */
public class AuthorizationPersistenceException extends RuntimeException {
    public AuthorizationPersistenceException(String message) {
        super(message);
    }

    public AuthorizationPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
