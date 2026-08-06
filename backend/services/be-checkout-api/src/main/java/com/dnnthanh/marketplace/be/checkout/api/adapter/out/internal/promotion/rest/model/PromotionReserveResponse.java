package com.dnnthanh.marketplace.be.checkout.api.adapter.out.internal.promotion.rest.model;

import java.math.BigDecimal;
import java.util.List;

public record PromotionReserveResponse(BigDecimal totalDiscount, List<String> promotionIds) {}
