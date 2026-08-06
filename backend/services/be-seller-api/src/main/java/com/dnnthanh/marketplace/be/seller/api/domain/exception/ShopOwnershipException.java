package com.dnnthanh.marketplace.be.seller.api.domain.exception;

/** Shop does not belong to the requested seller scope. */
public final class ShopOwnershipException extends RuntimeException {
    public ShopOwnershipException(String message) {
        super(message);
    }
}
