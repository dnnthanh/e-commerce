package com.dnnthanh.marketplace.be.catalog.api.api;

import com.dnnthanh.marketplace.be.catalog.api.api.request.CreateProductRequest;
import com.dnnthanh.marketplace.be.catalog.api.api.response.ProductResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/private/products")
public interface ProductPrivateApi {

    @PostMapping
    @PreAuthorize(
            "@authorizationService.hasSellerPermission('PRODUCT_CREATE', #request.sellerId())")
    ProductResponse create(@Valid @RequestBody CreateProductRequest request);

    @PostMapping("/{productId}/publish")
    @PreAuthorize("@authorizationService.hasPermission('PRODUCT_PUBLISH')")
    ProductResponse publish(@PathVariable Long productId);
}
