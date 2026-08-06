package com.dnnthanh.marketplace.be.pricing.api.application.port.in;

import com.dnnthanh.marketplace.be.pricing.api.application.query.PriceQueryCriteria;
import com.dnnthanh.marketplace.be.pricing.api.application.query.PriceResult;

/** Inbound effective-price use case. */
public interface PricingUseCase {
    PriceResult price(PriceQueryCriteria criteria);
}
