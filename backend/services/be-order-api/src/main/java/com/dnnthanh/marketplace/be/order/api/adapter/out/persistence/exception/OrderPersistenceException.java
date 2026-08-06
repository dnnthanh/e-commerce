package com.dnnthanh.marketplace.be.order.api.adapter.out.persistence.exception;

/** Infrastructure error raised when Order persistence serialization or mapping fails. */
public class OrderPersistenceException extends RuntimeException {
    public OrderPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
