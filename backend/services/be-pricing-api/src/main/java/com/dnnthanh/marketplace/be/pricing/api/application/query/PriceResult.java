package com.dnnthanh.marketplace.be.pricing.api.application.query;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PriceResult(
        Long skuId,
        Long sellerId,
        BigDecimal amount,
        String currency,
        Long ruleId,
        LocalDateTime resolvedAt) {}
