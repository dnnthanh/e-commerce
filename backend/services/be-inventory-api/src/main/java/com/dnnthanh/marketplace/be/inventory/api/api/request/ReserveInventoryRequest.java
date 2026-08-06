package com.dnnthanh.marketplace.be.inventory.api.api.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReserveInventoryRequest(
        @NotBlank String reservationKey,
        @NotNull Long skuId,
        @NotNull Long warehouseId,
        @Positive long quantity,
        @Min(1) @Max(120) int ttlMinutes) {}
