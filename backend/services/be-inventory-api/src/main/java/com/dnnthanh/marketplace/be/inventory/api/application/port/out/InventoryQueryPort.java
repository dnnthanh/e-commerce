package com.dnnthanh.marketplace.be.inventory.api.application.port.out;

import com.dnnthanh.marketplace.be.inventory.api.application.query.InventoryBalanceQueryResult;
import com.dnnthanh.marketplace.be.inventory.api.application.query.InventoryBalanceSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Read-model persistence port for inventory queries. */
public interface InventoryQueryPort {
    Page<InventoryBalanceQueryResult> findBalances(
            InventoryBalanceSearchCriteria criteria, Pageable pageable);

    long available(Long skuId);
}
