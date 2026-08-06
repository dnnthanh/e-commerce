package com.dnnthanh.marketplace.be.pricing.api.adapter.out.persistence.repository;

import com.dnnthanh.marketplace.be.pricing.api.adapter.out.persistence.entity.PriceRuleJpaEntity;
import com.dnnthanh.marketplace.be.pricing.api.application.query.PriceCandidateCriteria;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Effective-price candidate selection uses native SQL with one grouped SpEL criteria object. */
public interface PriceRuleJpaRepository extends JpaRepository<PriceRuleJpaEntity, Long> {
    @Query(
            value =
                    """
      SELECT pr.*
      FROM price_rule pr
      WHERE pr.sku_id = :#{#criteria.skuId}
        AND pr.status = 'ACTIVE'
        AND pr.valid_from <= :#{#criteria.at}
        AND (pr.valid_to IS NULL OR pr.valid_to > :#{#criteria.at})
        AND (pr.seller_id IS NULL OR pr.seller_id = :#{#criteria.sellerId})
        AND (pr.channel IS NULL OR pr.channel = :#{#criteria.channel})
      ORDER BY pr.priority DESC, pr.id DESC
      """,
            nativeQuery = true)
    List<PriceRuleJpaEntity> findEffectiveCandidatesNative(
            @Param("criteria") PriceCandidateCriteria criteria);
}
