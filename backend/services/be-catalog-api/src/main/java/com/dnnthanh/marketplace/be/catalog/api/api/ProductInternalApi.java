package com.dnnthanh.marketplace.be.catalog.api.api;

import com.dnnthanh.marketplace.be.catalog.api.api.response.SkuOwnerView;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/** Internal catalog lookup contract for service-to-service validation. */
@RequestMapping("/internal/catalog")
public interface ProductInternalApi {
    @GetMapping("/skus/{skuId}")
    @PreAuthorize("@internalServiceAuthorization.isServiceAccount()")
    SkuOwnerView sku(@PathVariable Long skuId);

    /** Authoritative SKU facts used by Cart/Checkout without exposing Catalog persistence. */
}
