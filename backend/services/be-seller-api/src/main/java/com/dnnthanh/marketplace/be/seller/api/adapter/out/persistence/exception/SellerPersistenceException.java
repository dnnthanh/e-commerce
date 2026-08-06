package com.dnnthanh.marketplace.be.seller.api.adapter.out.persistence.exception;

/** Infrastructure failure while persisting seller state/history/outbox data. */
public class SellerPersistenceException extends RuntimeException {
    public SellerPersistenceException(String message) {
        super(message);
    }

    public SellerPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
