package com.dnnthanh.marketplace.be.inventory.api.adapter.out.persistence.mapper;

import com.dnnthanh.marketplace.be.inventory.api.adapter.out.persistence.projection.InventoryBalanceProjection;
import com.dnnthanh.marketplace.be.inventory.api.application.query.InventoryBalanceQueryResult;
import com.dnnthanh.marketplace.be.platform.mapping.MapperContract;
import com.dnnthanh.marketplace.be.platform.mapping.PlatformMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = PlatformMapperConfig.class)
public interface InventoryQueryPersistenceMapper extends MapperContract {
    InventoryBalanceQueryResult toResult(InventoryBalanceProjection projection);
}
