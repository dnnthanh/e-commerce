package com.dnnthanh.marketplace.be.catalog.api.api.request.search;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Grouped public Catalog search request. */
@Getter
@Setter
@NoArgsConstructor
public class ProductSearchRequest {
    private String keyword;
    private Long sellerId;
    private Long categoryId;
}
