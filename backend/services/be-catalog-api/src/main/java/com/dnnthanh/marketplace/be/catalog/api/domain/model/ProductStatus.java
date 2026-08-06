package com.dnnthanh.marketplace.be.catalog.api.domain.model;

import com.dnnthanh.marketplace.be.platform.model.CodeEnum;

/** Product publication lifecycle. */
public enum ProductStatus implements CodeEnum {
    DRAFT,
    PUBLISHED,
    SUSPENDED,
    ARCHIVED
}
