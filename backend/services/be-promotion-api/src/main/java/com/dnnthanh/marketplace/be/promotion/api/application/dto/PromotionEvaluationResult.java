package com.dnnthanh.marketplace.be.promotion.api.application.dto;

import java.math.BigDecimal;
import java.util.List;

/** Explainable promotion evaluation result independent of HTTP transport. */
public record PromotionEvaluationResult(
        BigDecimal subtotal,
        BigDecimal totalDiscount,
        BigDecimal payable,
        List<AppliedPromotionDto> applied) {

    public PromotionEvaluationResult {
        applied = applied == null ? List.of() : List.copyOf(applied);
    }
}
