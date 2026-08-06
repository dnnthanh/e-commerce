package com.dnnthanh.marketplace.be.promotion.api.application.dto;

import java.math.BigDecimal;
import java.util.List;

/** Promotion reservation result returned to Checkout. */
public record PromotionReservationResult(
        BigDecimal totalDiscount, List<AppliedPromotionDto> applied) {

    public PromotionReservationResult {
        applied = applied == null ? List.of() : List.copyOf(applied);
    }
}
