package com.dnnthanh.marketplace.be.inventory.api.application.service;

import com.dnnthanh.marketplace.be.inventory.api.application.port.in.InventoryQueryUseCase;
import com.dnnthanh.marketplace.be.inventory.api.application.port.out.InventoryQueryPort;
import com.dnnthanh.marketplace.be.inventory.api.application.query.InventoryBalanceQueryResult;
import com.dnnthanh.marketplace.be.inventory.api.application.query.InventoryBalanceSearchCriteria;
import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

/** Read-only inventory operations use-case implementation. */
@UseCase
@RequiredArgsConstructor
public class InventoryQueryServiceImplement implements InventoryQueryUseCase {
    private final InventoryQueryPort inventoryQueryPort;

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryBalanceQueryResult> balances(
            InventoryBalanceSearchCriteria criteria, Pageable pageable) {
        return inventoryQueryPort.findBalances(criteria, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public long available(Long skuId) {
        return inventoryQueryPort.available(skuId);
    }
}
