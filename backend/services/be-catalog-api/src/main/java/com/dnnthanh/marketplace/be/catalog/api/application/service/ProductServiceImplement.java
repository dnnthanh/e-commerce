package com.dnnthanh.marketplace.be.catalog.api.application.service;

import com.dnnthanh.marketplace.be.catalog.api.application.exception.ProductNotFoundException;
import com.dnnthanh.marketplace.be.catalog.api.application.port.in.ProductUseCase;
import com.dnnthanh.marketplace.be.catalog.api.application.port.out.MediaReadinessPort;
import com.dnnthanh.marketplace.be.catalog.api.application.port.out.ProductRepositoryPort;
import com.dnnthanh.marketplace.be.catalog.api.application.query.ProductSearchCriteria;
import com.dnnthanh.marketplace.be.catalog.api.domain.model.Product;
import com.dnnthanh.marketplace.be.catalog.api.domain.model.ProductStatus;
import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

/** Catalog commands and queries around the canonical Product aggregate. */
@UseCase
@RequiredArgsConstructor
public class ProductServiceImplement implements ProductUseCase {

    private final ProductRepositoryPort repository;
    private final MediaReadinessPort mediaReadiness;

    @Transactional
    public Product create(Long sellerId, Long categoryId, String name, String description) {
        return repository.save(new Product(sellerId, categoryId, name, description));
    }

    /** Publishes a product after validating downstream Media readiness. */
    @Transactional
    public Product publish(Long productId) {
        Product product = requireProduct(productId);
        product.publish(mediaReadiness.isReady(productId));
        return repository.save(product);
    }

    /** Returns one published product without leaking draft/suspended existence. */
    @Transactional(readOnly = true)
    public Product getPublished(Long productId) {
        Product product = requireProduct(productId);
        if (product.status() != ProductStatus.PUBLISHED) {
            throw new ProductNotFoundException();
        }
        return product;
    }

    /** Public product search with bounded page size. */
    @Transactional(readOnly = true)
    public Page<Product> search(ProductSearchCriteria criteria, Pageable pageable) {
        return repository.search(criteria, pageable);
    }

    private Product requireProduct(Long productId) {
        return repository.findById(productId).orElseThrow(ProductNotFoundException::new);
    }
}
