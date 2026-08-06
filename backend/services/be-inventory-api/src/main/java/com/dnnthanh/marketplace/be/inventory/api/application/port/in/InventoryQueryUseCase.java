package com.dnnthanh.marketplace.be.inventory.api.application.port.in;

import com.dnnthanh.marketplace.be.inventory.api.application.query.InventoryBalanceQueryResult;
import com.dnnthanh.marketplace.be.inventory.api.application.query.InventoryBalanceSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Inventory read-model input port. */
public interface InventoryQueryUseCase {
    Page<InventoryBalanceQueryResult> balances(
            InventoryBalanceSearchCriteria criteria, Pageable pageable);

    long available(Long skuId);
}
