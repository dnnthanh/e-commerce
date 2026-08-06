package com.dnnthanh.marketplace.be.seller.api.domain.exception;

/** Shop definition violates seller invariants. */
public final class InvalidShopException extends RuntimeException {
    public InvalidShopException(String message) {
        super(message);
    }
}
