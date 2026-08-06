package com.dnnthanh.marketplace.be.promotion.api.api.response;

import java.math.BigDecimal;
import java.util.List;

public record ReservePromotionResponse(
        BigDecimal totalDiscount, List<String> promotionIds, List<AppliedPromotion> applied) {}
