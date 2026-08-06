package com.dnnthanh.marketplace.be.promotion.api.api.response;

import java.math.BigDecimal;
import java.util.List;

public record PromotionResult(
        BigDecimal subtotal,
        BigDecimal totalDiscount,
        BigDecimal payable,
        List<AppliedPromotion> applied) {}
