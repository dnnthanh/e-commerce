package com.dnnthanh.marketplace.be.cart.api.application.port.out;

import java.math.BigDecimal;

/** Authoritative Pricing quote required before Checkout. */
public interface PricingSnapshotPort {
    PriceSnapshot quote(String sku, Long sellerId, String channel);

    record PriceSnapshot(String sku, Long sellerId, BigDecimal amount, String currency) {}
}
