package com.dnnthanh.marketplace.be.seller.api.domain.exception;

/** Seller onboarding/operations command conflicts with lifecycle state. */
public final class SellerStateConflictException extends RuntimeException {
    public SellerStateConflictException(String message) {
        super(message);
    }
}
