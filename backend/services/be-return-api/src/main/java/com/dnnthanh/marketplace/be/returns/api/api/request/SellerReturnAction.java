package com.dnnthanh.marketplace.be.returns.api.api.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record SellerReturnAction(
        @NotNull @Positive Long sellerId, @Size(max = 500) String reason) {}
