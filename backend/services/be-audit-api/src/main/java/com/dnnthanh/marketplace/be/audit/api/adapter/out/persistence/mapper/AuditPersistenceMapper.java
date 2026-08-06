package com.dnnthanh.marketplace.be.audit.api.adapter.out.persistence.mapper;

import com.dnnthanh.marketplace.be.audit.api.adapter.out.persistence.projection.AuditSearchProjection;
import com.dnnthanh.marketplace.be.audit.api.application.query.AuditQueryResult;
import com.dnnthanh.marketplace.be.platform.mapping.MapperContract;
import com.dnnthanh.marketplace.be.platform.mapping.PlatformMapperConfig;
import org.mapstruct.Mapper;

/** Maps native projections to application read models. */
@Mapper(config = PlatformMapperConfig.class)
public interface AuditPersistenceMapper extends MapperContract {
    AuditQueryResult toResult(AuditSearchProjection projection);
}
