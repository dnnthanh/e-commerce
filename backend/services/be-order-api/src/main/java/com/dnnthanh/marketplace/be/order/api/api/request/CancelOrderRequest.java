package com.dnnthanh.marketplace.be.order.api.api.request;

import com.dnnthanh.marketplace.be.order.api.domain.enumtype.CancellationReason;
import jakarta.validation.constraints.NotNull;

/** Customer order cancellation request. */
public record CancelOrderRequest(@NotNull CancellationReason reason) {}
