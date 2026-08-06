package com.dnnthanh.marketplace.be.payment.api.domain.exception;

/** Provider/payment command conflicts with current payment lifecycle state. */
public final class PaymentStateConflictException extends RuntimeException {
    public PaymentStateConflictException(String message) {
        super(message);
    }
}
