package com.dnnthanh.marketplace.be.catalog.api.adapter.out.persistence.repository;

import com.dnnthanh.marketplace.be.catalog.api.adapter.out.persistence.entity.ProductJpaEntity;
import com.dnnthanh.marketplace.be.catalog.api.application.query.ProductSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Spring Data repository for Catalog CRUD and native relational search. */
public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, Long> {
    @Query(
            value =
                    """
      SELECT p.*
      FROM product p
      WHERE p.status = 'PUBLISHED'
        AND (:#{#criteria.keyword} IS NULL OR :#{#criteria.keyword} = ''
             OR LOWER(p.name) LIKE LOWER(CONCAT('%', :#{#criteria.keyword}, '%')))
        AND (:#{#criteria.sellerId} IS NULL OR p.seller_id = :#{#criteria.sellerId})
        AND (:#{#criteria.categoryId} IS NULL OR p.category_id = :#{#criteria.categoryId})
      ORDER BY p.updated_at DESC, p.id DESC
      """,
            countQuery =
                    """
      SELECT COUNT(*)
      FROM product p
      WHERE p.status = 'PUBLISHED'
        AND (:#{#criteria.keyword} IS NULL OR :#{#criteria.keyword} = ''
             OR LOWER(p.name) LIKE LOWER(CONCAT('%', :#{#criteria.keyword}, '%')))
        AND (:#{#criteria.sellerId} IS NULL OR p.seller_id = :#{#criteria.sellerId})
        AND (:#{#criteria.categoryId} IS NULL OR p.category_id = :#{#criteria.categoryId})
      """,
            nativeQuery = true)
    Page<ProductJpaEntity> searchPublishedNative(
            @Param("criteria") ProductSearchCriteria criteria, Pageable pageable);
}
