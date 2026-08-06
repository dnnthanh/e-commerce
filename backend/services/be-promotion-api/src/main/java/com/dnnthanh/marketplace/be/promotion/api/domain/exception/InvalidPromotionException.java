package com.dnnthanh.marketplace.be.promotion.api.domain.exception;

/** Promotion definition violates value/window/usage invariants. */
public final class InvalidPromotionException extends RuntimeException {
    public InvalidPromotionException(String message) {
        super(message);
    }
}
