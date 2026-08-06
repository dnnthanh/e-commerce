package com.dnnthanh.marketplace.be.checkout.api.application.model;

import java.math.BigDecimal;
import java.util.List;

public record PromotionReservation(BigDecimal totalDiscount, List<String> promotionIds) {
    public PromotionReservation {
        promotionIds = promotionIds == null ? List.of() : List.copyOf(promotionIds);
    }
}
