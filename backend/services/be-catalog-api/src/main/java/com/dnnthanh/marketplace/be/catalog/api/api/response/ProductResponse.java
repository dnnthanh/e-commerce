package com.dnnthanh.marketplace.be.catalog.api.api.response;

import com.dnnthanh.marketplace.be.catalog.api.domain.model.ProductStatus;
import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        Long sellerId,
        Long categoryId,
        String name,
        String description,
        ProductStatus status,
        LocalDateTime updatedAt) {}
