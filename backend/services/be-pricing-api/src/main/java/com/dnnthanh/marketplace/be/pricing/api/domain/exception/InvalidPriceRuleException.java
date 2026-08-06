package com.dnnthanh.marketplace.be.pricing.api.domain.exception;

/** Price rule violates money or effective-window invariants. */
public final class InvalidPriceRuleException extends RuntimeException {
    public InvalidPriceRuleException(String message) {
        super(message);
    }
}
