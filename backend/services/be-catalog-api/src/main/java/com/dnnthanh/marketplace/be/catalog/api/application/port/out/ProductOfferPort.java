package com.dnnthanh.marketplace.be.catalog.api.application.port.out;

import com.dnnthanh.marketplace.be.catalog.api.application.dto.ProductOffer;
import java.util.List;
import java.util.Optional;

/** Read projection boundary for public sellable product offers. */
public interface ProductOfferPort {
    List<ProductOffer> findSellableByProductId(Long productId);

    Optional<ProductOffer> findSellableBySkuId(Long skuId);
}
