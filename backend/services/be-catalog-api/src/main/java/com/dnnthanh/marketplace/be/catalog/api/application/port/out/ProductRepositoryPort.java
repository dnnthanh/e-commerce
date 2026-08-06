package com.dnnthanh.marketplace.be.catalog.api.application.port.out;

import com.dnnthanh.marketplace.be.catalog.api.application.query.ProductSearchCriteria;
import com.dnnthanh.marketplace.be.catalog.api.domain.model.Product;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Persistence port owned by the Catalog application layer. */
public interface ProductRepositoryPort {

    /**
     * Saves a product aggregate.
     *
     * @param product product aggregate
     * @return persisted product
     */
    Product save(Product product);

    /**
     * Finds a product by identifier.
     *
     * @param id product identifier
     * @return optional product
     */
    Optional<Product> findById(Long id);

    /** Searches published products using one grouped criteria object. */
    Page<Product> search(ProductSearchCriteria criteria, Pageable pageable);
}
