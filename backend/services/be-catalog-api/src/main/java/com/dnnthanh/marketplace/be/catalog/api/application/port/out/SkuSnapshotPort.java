package com.dnnthanh.marketplace.be.catalog.api.application.port.out;

import com.dnnthanh.marketplace.be.catalog.api.application.dto.SkuCheckoutSnapshot;
import java.util.Optional;

/** Read port for the small authoritative SKU snapshot consumed by internal flows. */
public interface SkuSnapshotPort {

    /**
     * Finds checkout-relevant SKU facts without exposing persistence to the web adapter.
     *
     * @param skuId SKU identifier
     * @return snapshot when the SKU exists
     */
    Optional<SkuCheckoutSnapshot> findBySkuId(Long skuId);
}
