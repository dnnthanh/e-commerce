package com.dnnthanh.marketplace.be.catalog.api.adapter.out.persistence;

import com.dnnthanh.marketplace.be.catalog.api.adapter.out.persistence.entity.ProductJpaEntity;
import com.dnnthanh.marketplace.be.catalog.api.adapter.out.persistence.repository.ProductJpaRepository;
import com.dnnthanh.marketplace.be.catalog.api.application.port.out.ProductRepositoryPort;
import com.dnnthanh.marketplace.be.catalog.api.application.query.ProductSearchCriteria;
import com.dnnthanh.marketplace.be.catalog.api.domain.exception.ProductStateConflictException;
import com.dnnthanh.marketplace.be.catalog.api.domain.model.Product;
import com.dnnthanh.marketplace.be.platform.stereotype.Persistence;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * Product aggregate persistence. Ordinary CRUD uses Spring Data JPA, relational search uses a
 * repository native SQL query, and JdbcClient is retained only for the PostgreSQL JSONB outbox
 * insert that must share the same local transaction as the aggregate mutation.
 */
@Persistence
@RequiredArgsConstructor
public class ProductPersistenceAdapter implements ProductRepositoryPort {

    private final ProductJpaRepository repository;
    private final JdbcClient jdbc;

    /** {@inheritDoc} */
    @Override
    public Product save(Product product) {
        try {
            ProductJpaEntity entity =
                    product.id() == null
                            ? ProductJpaEntity.fromNew(product)
                            : requireCurrentEntity(product);
            if (product.id() != null) {
                entity.apply(product);
            }
            Product persisted = repository.saveAndFlush(entity).toDomain();
            appendChangedEvent(persisted);
            return persisted;
        } catch (OptimisticLockingFailureException conflict) {
            throw new ProductStateConflictException(
                    "Product was concurrently modified: " + product.id());
        }
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Product> findById(Long id) {
        return repository.findById(id).map(ProductJpaEntity::toDomain);
    }

    /** {@inheritDoc} */
    @Override
    public Page<Product> search(ProductSearchCriteria criteria, Pageable pageable) {
        return repository.searchPublishedNative(criteria, pageable).map(ProductJpaEntity::toDomain);
    }

    private ProductJpaEntity requireCurrentEntity(Product product) {
        ProductJpaEntity entity =
                repository
                        .findById(product.id())
                        .orElseThrow(
                                () ->
                                        new ProductStateConflictException(
                                                "Persisted product could not be reloaded: "
                                                        + product.id()));
        if (entity.getVersion() != product.version()) {
            throw new ProductStateConflictException(
                    "Product was concurrently modified: " + product.id());
        }
        return entity;
    }

    private void appendChangedEvent(Product product) {
        jdbc.sql(
                        """
            INSERT INTO outbox_event(event_id,aggregate_id,event_type,payload_json,status,created_at)
            VALUES(:event,:aggregate,'PRODUCT_CHANGED',jsonb_build_object(
                'productId',:product,'sellerId',:seller,'categoryId',:category,'name',:name,'status',:status),
                'PENDING',now())
            """)
                .param("event", UUID.randomUUID().toString())
                .param("aggregate", String.valueOf(product.id()))
                .param("product", product.id())
                .param("seller", product.sellerId())
                .param("category", product.categoryId())
                .param("name", product.name())
                .param("status", product.status().name())
                .update();
    }
}
