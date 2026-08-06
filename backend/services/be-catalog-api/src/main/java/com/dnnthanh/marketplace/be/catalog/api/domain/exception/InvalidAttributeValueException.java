package com.dnnthanh.marketplace.be.catalog.api.domain.exception;

/** Product dynamic attribute value violates its typed definition. */
public final class InvalidAttributeValueException extends RuntimeException {
    public InvalidAttributeValueException(String code) {
        super("Invalid value for attribute " + code);
    }

    public InvalidAttributeValueException(String code, String message) {
        super(message + ": " + code);
    }
}
