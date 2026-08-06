package com.dnnthanh.marketplace.be.promotion.api.api.request;

import java.math.BigDecimal;
import java.util.List;

public record ReservePromotionRequest(
        String checkoutKey, String customerId, BigDecimal subtotal, List<String> codes) {}
