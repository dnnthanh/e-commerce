package com.dnnthanh.marketplace.be.pricing.api.domain.exception;

/** No active price rule can serve the requested pricing context. */
public final class EffectivePriceNotFoundException extends RuntimeException {
    public EffectivePriceNotFoundException(String sku) {
        super("No effective price for " + sku);
    }
}
