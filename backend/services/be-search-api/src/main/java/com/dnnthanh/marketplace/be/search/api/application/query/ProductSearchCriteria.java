package com.dnnthanh.marketplace.be.search.api.application.query;

import com.dnnthanh.marketplace.be.platform.model.CodeEnum;
import com.dnnthanh.marketplace.be.search.api.application.exception.InvalidSearchCriteriaException;
import java.math.BigDecimal;
import java.util.Set;

/** Search-engine independent product search criteria using cursor/search-after pagination. */
public record ProductSearchCriteria(
        String keyword,
        Set<Long> categoryIds,
        Set<Long> brandIds,
        Set<Long> sellerIds,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        boolean inStockOnly,
        Sort sort,
        String cursor,
        int size) {
    public ProductSearchCriteria {
        categoryIds = categoryIds == null ? Set.of() : Set.copyOf(categoryIds);
        brandIds = brandIds == null ? Set.of() : Set.copyOf(brandIds);
        sellerIds = sellerIds == null ? Set.of() : Set.copyOf(sellerIds);
        sort = sort == null ? Sort.RELEVANCE : sort;
        if (size < 1 || size > 100)
            throw new InvalidSearchCriteriaException("Search page size must be 1..100");
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0)
            throw new InvalidSearchCriteriaException("Price range is invalid");
    }

    public enum Sort implements CodeEnum {
        RELEVANCE,
        PRICE_ASC,
        PRICE_DESC,
        NEWEST,
        POPULARITY
    }
}
