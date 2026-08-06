package com.dnnthanh.marketplace.be.catalog.api.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateProductRequest(
        @NotNull Long sellerId,
        @NotNull Long categoryId,
        @NotBlank String name,
        String description) {}
