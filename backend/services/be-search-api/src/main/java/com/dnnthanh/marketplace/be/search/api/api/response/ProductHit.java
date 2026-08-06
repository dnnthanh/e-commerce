package com.dnnthanh.marketplace.be.search.api.api.response;

import java.math.BigDecimal;

public record ProductHit(
        Long productId, Long sellerId, String name, BigDecimal price, double rating) {}
