package com.dnnthanh.marketplace.be.catalog.api.application.port.out;

/** Catalog-to-Media anti-corruption port. */
public interface MediaReadinessPort {

    /**
     * Checks whether storefront-required variants exist. @param productId product id @return
     * readiness
     */
    boolean isReady(Long productId);
}
