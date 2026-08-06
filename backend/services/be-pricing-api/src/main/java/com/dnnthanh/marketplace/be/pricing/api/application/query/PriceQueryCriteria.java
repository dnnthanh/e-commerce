package com.dnnthanh.marketplace.be.pricing.api.application.query;

import java.time.LocalDateTime;

public record PriceQueryCriteria(Long skuId, Long sellerId, String channel, LocalDateTime at) {}
