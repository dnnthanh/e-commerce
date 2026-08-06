package com.dnnthanh.marketplace.be.order.api.domain.exception;

/** Raised when an order command violates aggregate input invariants. */
public class InvalidOrderValidationException extends RuntimeException {
    public InvalidOrderValidationException(String message) {
        super(message);
    }
}
