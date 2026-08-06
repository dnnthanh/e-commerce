package com.dnnthanh.marketplace.be.checkout.api.domain.exception;

/** Checkout process-manager command is illegal for the persisted workflow state. */
public final class CheckoutStateConflictException extends RuntimeException {
    public CheckoutStateConflictException(String message) {
        super(message);
    }
}
