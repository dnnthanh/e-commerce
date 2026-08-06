package com.dnnthanh.marketplace.be.search.api.application.port.in;

import com.dnnthanh.marketplace.be.search.api.application.port.out.ProductSearchPort;
import com.dnnthanh.marketplace.be.search.api.application.query.ProductSearchCriteria;

public interface ProductSearchUseCase {
    ProductSearchPort.SearchPage search(ProductSearchCriteria criteria);
}
