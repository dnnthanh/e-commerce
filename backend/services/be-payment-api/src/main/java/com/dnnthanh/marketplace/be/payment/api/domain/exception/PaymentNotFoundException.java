package com.dnnthanh.marketplace.be.payment.api.domain.exception;

/** Requested payment does not exist. */
public final class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(String message) {
        super(message);
    }
}
