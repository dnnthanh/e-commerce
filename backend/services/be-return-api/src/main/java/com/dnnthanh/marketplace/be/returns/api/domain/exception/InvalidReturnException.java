package com.dnnthanh.marketplace.be.returns.api.domain.exception;

/** Return request/inspection violates quantity or refund invariants. */
public final class InvalidReturnException extends RuntimeException {
    public InvalidReturnException(String message) {
        super(message);
    }
}
