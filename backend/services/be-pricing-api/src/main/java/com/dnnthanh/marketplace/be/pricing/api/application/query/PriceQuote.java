package com.dnnthanh.marketplace.be.pricing.api.application.query;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PriceQuote(
        String quoteId,
        String sku,
        Long sellerId,
        String channel,
        String currency,
        BigDecimal amount,
        String sourceRuleId,
        LocalDateTime quotedAt) {}
