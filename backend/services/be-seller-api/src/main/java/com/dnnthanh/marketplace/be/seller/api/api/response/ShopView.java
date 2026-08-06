package com.dnnthanh.marketplace.be.seller.api.api.response;

import com.dnnthanh.marketplace.be.seller.api.domain.enumtype.SellerStatus;
import java.time.LocalDateTime;

public record ShopView(
        Long id,
        Long sellerId,
        String slug,
        String name,
        String description,
        SellerStatus status,
        LocalDateTime updatedAt) {}
