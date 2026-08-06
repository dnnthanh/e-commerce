package com.dnnthanh.marketplace.be.inventory.worker.exception;

/** Malformed durable Return event that cannot be safely applied to inventory. */
public final class InvalidReturnRestockEventException extends RuntimeException {
    public InvalidReturnRestockEventException(String message) {
        super(message);
    }
}
