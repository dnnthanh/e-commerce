package com.dnnthanh.marketplace.be.returns.api.domain.exception;

/** Return command conflicts with the current return workflow state. */
public final class ReturnStateConflictException extends RuntimeException {
    public ReturnStateConflictException(String message) {
        super(message);
    }
}
