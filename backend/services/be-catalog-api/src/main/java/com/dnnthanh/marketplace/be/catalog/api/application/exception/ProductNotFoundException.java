package com.dnnthanh.marketplace.be.catalog.api.application.exception;

import com.dnnthanh.marketplace.be.platform.exception.BusinessException;

/** Stable not-found error for products and SKUs hidden behind Catalog use cases. */
public final class ProductNotFoundException extends BusinessException {

    public ProductNotFoundException() {
        super(CatalogErrorCode.PRODUCT_NOT_FOUND);
    }
}
