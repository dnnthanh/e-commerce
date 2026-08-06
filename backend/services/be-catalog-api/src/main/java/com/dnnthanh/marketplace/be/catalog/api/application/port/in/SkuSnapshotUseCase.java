package com.dnnthanh.marketplace.be.catalog.api.application.port.in;

import com.dnnthanh.marketplace.be.catalog.api.application.dto.SkuCheckoutSnapshot;

public interface SkuSnapshotUseCase {
    SkuCheckoutSnapshot get(Long skuId);
}
