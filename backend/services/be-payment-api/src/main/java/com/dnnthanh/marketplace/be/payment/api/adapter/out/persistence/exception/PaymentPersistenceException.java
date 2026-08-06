package com.dnnthanh.marketplace.be.payment.api.adapter.out.persistence.exception;

/** Payment database/outbox serialization failure. */
public final class PaymentPersistenceException extends RuntimeException {
    public PaymentPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
