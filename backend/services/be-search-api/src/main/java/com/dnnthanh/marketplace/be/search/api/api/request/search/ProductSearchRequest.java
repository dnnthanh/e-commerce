package com.dnnthanh.marketplace.be.search.api.api.request.search;

import com.dnnthanh.marketplace.be.search.api.application.query.ProductSearchCriteria.Sort;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Grouped HTTP search request for product discovery. */
@Getter
@Setter
@NoArgsConstructor
public class ProductSearchRequest {
    private String query;
    private Set<Long> sellerId;
    private Set<Long> categoryId;
    private Set<Long> brandId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private boolean inStockOnly;
    private Sort sort = Sort.RELEVANCE;
    private String cursor;

    @Min(1)
    @Max(100)
    private int size = 24;
}
