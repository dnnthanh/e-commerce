package com.dnnthanh.marketplace.be.returns.api.application.exception;

/** Signals that another request with the same idempotency key won the unique-key race. */
public final class ConcurrentReturnCreateException extends RuntimeException {
    public ConcurrentReturnCreateException(Throwable cause) {
        super("Concurrent return request already persisted", cause);
    }
}
