package com.dnnthanh.marketplace.be.returns.api.api.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReceiveReturnRequest(@NotNull @Positive Long warehouseId) {}
