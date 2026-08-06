package com.dnnthanh.marketplace.be.payment.api.domain.exception;

/** Payment state does not allow a refund. */
public final class PaymentNotRefundableException extends RuntimeException {
    public PaymentNotRefundableException(String message) {
        super(message);
    }
}
