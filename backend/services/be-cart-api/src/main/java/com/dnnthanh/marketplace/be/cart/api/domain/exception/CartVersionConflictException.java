package com.dnnthanh.marketplace.be.cart.api.domain.exception;

/** Optimistic cart version does not match the caller snapshot. */
public final class CartVersionConflictException extends RuntimeException {
    public CartVersionConflictException(long expected, long actual) {
        super("Cart version conflict: expected=" + expected + ", actual=" + actual);
    }
}
