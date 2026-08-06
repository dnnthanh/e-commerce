package com.dnnthanh.marketplace.be.payment.api.domain.exception;

/** Payment amount/input violates payment invariants. */
public final class InvalidPaymentException extends RuntimeException {
    public InvalidPaymentException(String message) {
        super(message);
    }
}
