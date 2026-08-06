package com.dnnthanh.marketplace.be.returns.api.api.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record InspectLineRequest(
        @NotNull @Positive Long orderLineId,
        @Min(0) @Max(1_000_000) int acceptedQuantity,
        @NotBlank String disposition) {}
