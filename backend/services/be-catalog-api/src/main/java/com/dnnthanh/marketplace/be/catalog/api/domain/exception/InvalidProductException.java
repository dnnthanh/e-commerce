package com.dnnthanh.marketplace.be.catalog.api.domain.exception;

/** Product definition violates catalog invariants. */
public final class InvalidProductException extends RuntimeException {
    public InvalidProductException(String message) {
        super(message);
    }
}
