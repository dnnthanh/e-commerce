package com.dnnthanh.marketplace.be.checkout.api.application.exception;

/** Persisted checkout request snapshot is missing or cannot be restored. */
public final class CheckoutSnapshotException extends RuntimeException {
    public CheckoutSnapshotException(String message) {
        super(message);
    }

    public CheckoutSnapshotException(String message, Throwable cause) {
        super(message, cause);
    }
}
