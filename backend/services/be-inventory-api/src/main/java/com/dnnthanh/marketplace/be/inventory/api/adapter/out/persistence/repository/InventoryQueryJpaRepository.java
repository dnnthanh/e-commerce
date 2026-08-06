package com.dnnthanh.marketplace.be.inventory.api.adapter.out.persistence.repository;

import com.dnnthanh.marketplace.be.inventory.api.adapter.out.persistence.entity.InventoryBalanceId;
import com.dnnthanh.marketplace.be.inventory.api.adapter.out.persistence.entity.InventoryBalanceJpaEntity;
import com.dnnthanh.marketplace.be.inventory.api.adapter.out.persistence.projection.InventoryBalanceProjection;
import com.dnnthanh.marketplace.be.inventory.api.application.query.InventoryBalanceSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/** Native SQL read repository; grouped business filters are separate from Pageable. */
public interface InventoryQueryJpaRepository
        extends Repository<InventoryBalanceJpaEntity, InventoryBalanceId> {

    @Query(
            value =
                    """
      SELECT sku_id AS skuId,
             warehouse_id AS warehouseId,
             on_hand AS onHand,
             reserved,
             (on_hand - reserved) AS available,
             version
      FROM inventory_balance
      WHERE (:#{#criteria.skuId} IS NULL OR sku_id = :#{#criteria.skuId})
        AND (:#{#criteria.warehouseId} IS NULL OR warehouse_id = :#{#criteria.warehouseId})
      ORDER BY sku_id, warehouse_id
      """,
            countQuery =
                    """
      SELECT COUNT(*)
      FROM inventory_balance
      WHERE (:#{#criteria.skuId} IS NULL OR sku_id = :#{#criteria.skuId})
        AND (:#{#criteria.warehouseId} IS NULL OR warehouse_id = :#{#criteria.warehouseId})
      """,
            nativeQuery = true)
    Page<InventoryBalanceProjection> search(
            @Param("criteria") InventoryBalanceSearchCriteria criteria, Pageable pageable);

    @Query(
            value =
                    "SELECT COALESCE(SUM(on_hand - reserved), 0) FROM inventory_balance WHERE sku_id = :skuId",
            nativeQuery = true)
    long available(@Param("skuId") Long skuId);
}
