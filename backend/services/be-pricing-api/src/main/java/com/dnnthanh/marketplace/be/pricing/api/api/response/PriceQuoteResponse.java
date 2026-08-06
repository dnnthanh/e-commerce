package com.dnnthanh.marketplace.be.pricing.api.api.response;

import java.math.BigDecimal;

public record PriceQuoteResponse(
        BigDecimal amount, String currency, String quoteId, String sourceRuleId) {}
