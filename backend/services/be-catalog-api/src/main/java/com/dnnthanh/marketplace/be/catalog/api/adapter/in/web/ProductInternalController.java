package com.dnnthanh.marketplace.be.catalog.api.adapter.in.web;

import com.dnnthanh.marketplace.be.catalog.api.adapter.in.web.mapper.ProductApiMapper;
import com.dnnthanh.marketplace.be.catalog.api.api.ProductInternalApi;
import com.dnnthanh.marketplace.be.catalog.api.api.response.SkuOwnerView;
import com.dnnthanh.marketplace.be.catalog.api.application.port.in.SkuSnapshotUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

/** Internal authoritative Catalog snapshots consumed by Cart/Checkout. */
@RestController
@RequiredArgsConstructor
public class ProductInternalController implements ProductInternalApi {
    private final SkuSnapshotUseCase snapshotUseCase;
    private final ProductApiMapper mapper;

    @Override
    public SkuOwnerView sku(Long skuId) {
        return mapper.toView(snapshotUseCase.get(skuId));
    }
}
