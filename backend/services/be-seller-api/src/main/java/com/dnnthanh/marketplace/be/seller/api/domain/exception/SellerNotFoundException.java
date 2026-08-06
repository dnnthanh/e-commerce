package com.dnnthanh.marketplace.be.seller.api.domain.exception;

/** Requested seller aggregate does not exist. */
public final class SellerNotFoundException extends RuntimeException {
    public SellerNotFoundException(String message) {
        super(message);
    }
}
