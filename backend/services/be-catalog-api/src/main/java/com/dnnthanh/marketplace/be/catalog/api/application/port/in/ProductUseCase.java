package com.dnnthanh.marketplace.be.catalog.api.application.port.in;

import com.dnnthanh.marketplace.be.catalog.api.application.query.ProductSearchCriteria;
import com.dnnthanh.marketplace.be.catalog.api.domain.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductUseCase {
    Product create(Long sellerId, Long categoryId, String name, String description);

    Product publish(Long productId);

    Product getPublished(Long productId);

    Page<Product> search(ProductSearchCriteria criteria, Pageable pageable);
}
