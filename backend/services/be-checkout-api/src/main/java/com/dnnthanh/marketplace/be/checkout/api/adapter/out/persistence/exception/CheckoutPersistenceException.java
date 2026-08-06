package com.dnnthanh.marketplace.be.checkout.api.adapter.out.persistence.exception;

/** Infrastructure failure while reading or writing a durable checkout workflow. */
public final class CheckoutPersistenceException extends RuntimeException {

    public CheckoutPersistenceException(String message) {
        super(message);
    }
}
