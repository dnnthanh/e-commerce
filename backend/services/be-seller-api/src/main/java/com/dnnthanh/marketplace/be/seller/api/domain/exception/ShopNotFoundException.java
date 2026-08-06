package com.dnnthanh.marketplace.be.seller.api.domain.exception;

/** Requested shop does not exist. */
public final class ShopNotFoundException extends RuntimeException {
    public ShopNotFoundException(String message) {
        super(message);
    }
}
