package com.dnnthanh.marketplace.be.seller.api.domain.model;

import com.dnnthanh.marketplace.be.seller.api.domain.enumtype.SellerStatus;
import com.dnnthanh.marketplace.be.seller.api.domain.exception.SellerStateConflictException;
import java.util.Objects;

/** Seller aggregate owning marketplace onboarding and lifecycle state only. */
public final class SellerAccount {
    private final Long sellerId;
    private SellerStatus status = SellerStatus.DRAFT;

    public SellerAccount(Long sellerId) {
        this.sellerId = Objects.requireNonNull(sellerId);
    }

    /** Rehydrates persisted onboarding state; authorization remains exclusively in Keycloak. */
    public static SellerAccount rehydrate(Long sellerId, SellerStatus status) {
        SellerAccount seller = new SellerAccount(sellerId);
        seller.status = Objects.requireNonNull(status);
        return seller;
    }

    public void submit() {
        require(SellerStatus.DRAFT);
        status = SellerStatus.SUBMITTED;
    }

    public void verify() {
        require(SellerStatus.SUBMITTED);
        status = SellerStatus.VERIFIED;
    }

    public void activate() {
        require(SellerStatus.VERIFIED);
        status = SellerStatus.ACTIVE;
    }

    public void suspend() {
        if (status != SellerStatus.ACTIVE) {
            throw new SellerStateConflictException("Only active seller can be suspended");
        }
        status = SellerStatus.SUSPENDED;
    }

    public void reinstate() {
        require(SellerStatus.SUSPENDED);
        status = SellerStatus.ACTIVE;
    }

    private void require(SellerStatus expected) {
        if (status != expected) {
            throw new SellerStateConflictException(
                    "Seller state conflict: expected=" + expected + ", actual=" + status);
        }
    }

    public SellerStatus status() {
        return status;
    }

    public Long sellerId() {
        return sellerId;
    }
}
