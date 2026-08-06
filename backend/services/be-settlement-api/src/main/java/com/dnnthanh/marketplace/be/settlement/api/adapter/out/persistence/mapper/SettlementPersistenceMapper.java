package com.dnnthanh.marketplace.be.settlement.api.adapter.out.persistence.mapper;

import com.dnnthanh.marketplace.be.platform.mapping.MapperContract;
import com.dnnthanh.marketplace.be.platform.mapping.PlatformMapperConfig;
import com.dnnthanh.marketplace.be.settlement.api.adapter.out.persistence.projection.SettlementSummaryProjection;
import com.dnnthanh.marketplace.be.settlement.api.application.query.SettlementQueryResult;
import org.mapstruct.Mapper;

/** Maps native settlement read projections into application query results. */
@Mapper(config = PlatformMapperConfig.class)
public interface SettlementPersistenceMapper extends MapperContract {
    SettlementQueryResult toQueryResult(SettlementSummaryProjection source);
}
