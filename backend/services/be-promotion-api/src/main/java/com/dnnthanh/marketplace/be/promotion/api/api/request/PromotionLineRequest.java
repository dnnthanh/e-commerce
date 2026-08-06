package com.dnnthanh.marketplace.be.promotion.api.api.request;

import java.math.BigDecimal;

public record PromotionLineRequest(
        Long sellerId, String skuId, Long categoryId, BigDecimal subtotal) {}
