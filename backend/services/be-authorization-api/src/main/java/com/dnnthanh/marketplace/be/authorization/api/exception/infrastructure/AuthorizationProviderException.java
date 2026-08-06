package com.dnnthanh.marketplace.be.authorization.api.exception.infrastructure;

/** Keycloak/admin-provider response is unavailable or inconsistent with the requested mutation. */
public final class AuthorizationProviderException extends RuntimeException {
    public AuthorizationProviderException(String message) {
        super(message);
    }

    public AuthorizationProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
