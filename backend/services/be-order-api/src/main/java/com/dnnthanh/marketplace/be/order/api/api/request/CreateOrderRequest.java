package com.dnnthanh.marketplace.be.order.api.api.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;

/** Internal checkout-to-order creation request. */
public record CreateOrderRequest(
        @NotBlank String checkoutKey,
        @NotBlank String userId,
        @NotEmpty List<@Valid LineRequest> lines,
        @NotNull @DecimalMin(value = "0.01") BigDecimal grossAmount,
        @NotNull @DecimalMin(value = "0.00") BigDecimal discountAmount) {

    /** Repriced immutable line snapshot from Checkout. */
    public record LineRequest(
            @NotNull Long sellerId,
            @NotNull Long skuId,
            @Positive int quantity,
            @NotNull @DecimalMin(value = "0.01") BigDecimal unitPrice) {}
}
