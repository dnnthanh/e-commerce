package com.dnnthanh.marketplace.be.cart.api.domain.exception;

/** Cart mutation violates quantity or merge policy. */
public final class InvalidCartMutationException extends RuntimeException {
    public InvalidCartMutationException(String message) {
        super(message);
    }
}
