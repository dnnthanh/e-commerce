package com.dnnthanh.marketplace.be.checkout.api.application.port.out;

import java.math.BigDecimal;

/** Pricing remote boundary. */
public interface PricingClientPort {

    BigDecimal price(Long skuId);
}
