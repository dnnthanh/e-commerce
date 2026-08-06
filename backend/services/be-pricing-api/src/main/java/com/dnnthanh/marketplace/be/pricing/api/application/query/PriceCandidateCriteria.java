package com.dnnthanh.marketplace.be.pricing.api.application.query;

import java.time.LocalDateTime;

/** Grouped criteria passed to native effective-price SQL. */
public record PriceCandidateCriteria(Long skuId, Long sellerId, String channel, LocalDateTime at) {}
