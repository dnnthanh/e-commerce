package com.dnnthanh.marketplace.be.pricing.api.api.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PriceView(
        Long skuId,
        Long sellerId,
        BigDecimal amount,
        String currency,
        Long ruleId,
        LocalDateTime resolvedAt) {}
