package com.dnnthanh.marketplace.be.seller.api.application.service;

import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import com.dnnthanh.marketplace.be.seller.api.application.port.out.SellerAccountPort;
import com.dnnthanh.marketplace.be.seller.api.domain.exception.SellerNotFoundException;
import com.dnnthanh.marketplace.be.seller.api.domain.model.SellerAccount;
import lombok.RequiredArgsConstructor;

/** Seller onboarding and suspension lifecycle commands. */
@UseCase
@RequiredArgsConstructor
public class SellerLifecycleService {
    private final SellerAccountPort repository;

    public SellerAccount verify(Long sellerId) {
        SellerAccount seller = load(sellerId);
        seller.verify();
        return repository.save(seller);
    }

    public SellerAccount activate(Long sellerId) {
        SellerAccount seller = load(sellerId);
        seller.activate();
        return repository.save(seller);
    }

    public SellerAccount suspend(Long sellerId) {
        SellerAccount seller = load(sellerId);
        seller.suspend();
        return repository.save(seller);
    }

    private SellerAccount load(Long sellerId) {
        return repository
                .findBySellerId(sellerId)
                .orElseThrow(() -> new SellerNotFoundException("Seller not found: " + sellerId));
    }
}
