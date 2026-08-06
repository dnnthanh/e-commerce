package com.dnnthanh.marketplace.be.catalog.api.domain.exception;

/** Variant-generating attribute lacks a valid selectable value set. */
public final class InvalidVariantDefinitionException extends RuntimeException {
    public InvalidVariantDefinitionException(String code) {
        super("Variant attribute requires values: " + code);
    }
}
