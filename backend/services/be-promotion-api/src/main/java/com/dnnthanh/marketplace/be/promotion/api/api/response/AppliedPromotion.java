package com.dnnthanh.marketplace.be.promotion.api.api.response;

import java.math.BigDecimal;

public record AppliedPromotion(String promotionId, String code, BigDecimal discount) {}
