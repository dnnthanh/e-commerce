package com.dnnthanh.marketplace.be.checkout.api.application.model;

import java.math.BigDecimal;

public record PricedOrderLine(Long sellerId, Long skuId, int quantity, BigDecimal unitPrice) {}
