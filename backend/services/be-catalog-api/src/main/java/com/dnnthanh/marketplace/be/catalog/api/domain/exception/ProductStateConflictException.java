package com.dnnthanh.marketplace.be.catalog.api.domain.exception;

/** Product command conflicts with the current publication lifecycle. */
public final class ProductStateConflictException extends RuntimeException {

    public ProductStateConflictException(String message) {
        super(message);
    }
}
