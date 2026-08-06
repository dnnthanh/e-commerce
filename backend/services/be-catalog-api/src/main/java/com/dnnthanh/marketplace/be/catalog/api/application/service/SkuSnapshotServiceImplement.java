package com.dnnthanh.marketplace.be.catalog.api.application.service;

import com.dnnthanh.marketplace.be.catalog.api.application.dto.SkuCheckoutSnapshot;
import com.dnnthanh.marketplace.be.catalog.api.application.exception.ProductNotFoundException;
import com.dnnthanh.marketplace.be.catalog.api.application.port.in.SkuSnapshotUseCase;
import com.dnnthanh.marketplace.be.catalog.api.application.port.out.SkuSnapshotPort;
import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

/** Resolves checkout-relevant SKU facts through the Catalog application boundary. */
@UseCase
@RequiredArgsConstructor
public class SkuSnapshotServiceImplement implements SkuSnapshotUseCase {

    private final SkuSnapshotPort snapshotPort;

    /**
     * Loads one authoritative SKU snapshot.
     *
     * @param skuId SKU identifier
     * @return checkout snapshot
     */
    @Transactional(readOnly = true)
    public SkuCheckoutSnapshot get(Long skuId) {
        return snapshotPort.findBySkuId(skuId).orElseThrow(ProductNotFoundException::new);
    }
}
