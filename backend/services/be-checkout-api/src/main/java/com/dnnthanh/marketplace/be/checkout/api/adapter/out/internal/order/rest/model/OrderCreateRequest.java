package com.dnnthanh.marketplace.be.checkout.api.adapter.out.internal.order.rest.model;

import com.dnnthanh.marketplace.be.checkout.api.application.model.PricedOrderLine;
import java.math.BigDecimal;
import java.util.List;

public record OrderCreateRequest(
        String checkoutKey,
        String userId,
        List<PricedOrderLine> lines,
        BigDecimal grossAmount,
        BigDecimal discountAmount) {}
