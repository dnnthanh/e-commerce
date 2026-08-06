package com.dnnthanh.marketplace.be.seller.api.adapter.out.persistence.repository;

import com.dnnthanh.marketplace.be.seller.api.adapter.out.persistence.entity.ShopJpaEntity;
import com.dnnthanh.marketplace.be.seller.api.application.query.SellerShopSearchCriteria;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Shop repository; list/search uses native SQL per repository policy. */
public interface ShopJpaRepository extends JpaRepository<ShopJpaEntity, Long> {
    @Query(
            value =
                    """
          SELECT *
          FROM shop
          WHERE seller_id = :#{#criteria.sellerId}
          ORDER BY id
          """,
            nativeQuery = true)
    List<ShopJpaEntity> findBySellerNative(@Param("criteria") SellerShopSearchCriteria criteria);
}
