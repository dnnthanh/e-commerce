package com.dnnthanh.marketplace.be.pricing.api.application.query;

import java.time.LocalDateTime;

public record PriceQuoteQuery(String sku, Long sellerId, String channel, LocalDateTime at) {}
