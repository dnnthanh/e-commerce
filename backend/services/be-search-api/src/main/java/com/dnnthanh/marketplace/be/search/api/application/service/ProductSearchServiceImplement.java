package com.dnnthanh.marketplace.be.search.api.application.service;

import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import com.dnnthanh.marketplace.be.search.api.application.port.in.ProductSearchUseCase;
import com.dnnthanh.marketplace.be.search.api.application.port.out.ProductSearchPort;
import com.dnnthanh.marketplace.be.search.api.application.query.ProductSearchCriteria;
import lombok.RequiredArgsConstructor;

/** Search query use case supporting cursor/search-after deep pagination. */
@UseCase
@RequiredArgsConstructor
public class ProductSearchServiceImplement implements ProductSearchUseCase {
    private final ProductSearchPort searchPort;

    public ProductSearchPort.SearchPage search(ProductSearchCriteria criteria) {
        return searchPort.search(criteria);
    }
}
