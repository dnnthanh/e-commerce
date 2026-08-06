package com.dnnthanh.marketplace.be.inventory.api.adapter.out.persistence;

import com.dnnthanh.marketplace.be.inventory.api.adapter.out.persistence.mapper.InventoryQueryPersistenceMapper;
import com.dnnthanh.marketplace.be.inventory.api.adapter.out.persistence.repository.InventoryQueryJpaRepository;
import com.dnnthanh.marketplace.be.inventory.api.application.port.out.InventoryQueryPort;
import com.dnnthanh.marketplace.be.inventory.api.application.query.InventoryBalanceQueryResult;
import com.dnnthanh.marketplace.be.inventory.api.application.query.InventoryBalanceSearchCriteria;
import com.dnnthanh.marketplace.be.platform.stereotype.Persistence;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** JPA/native-SQL adapter for ordinary inventory read/search queries. */
@Persistence
@RequiredArgsConstructor
public class InventoryQueryPersistenceAdapter implements InventoryQueryPort {
    private final InventoryQueryJpaRepository repository;
    private final InventoryQueryPersistenceMapper mapper;

    @Override
    public Page<InventoryBalanceQueryResult> findBalances(
            InventoryBalanceSearchCriteria criteria, Pageable pageable) {
        return repository.search(criteria, pageable).map(mapper::toResult);
    }

    @Override
    public long available(Long skuId) {
        return repository.available(skuId);
    }
}
