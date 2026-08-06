package com.dnnthanh.marketplace.be.operations.api.adapter.in.web.mapper;

import com.dnnthanh.marketplace.be.operations.api.api.response.IncidentView;
import com.dnnthanh.marketplace.be.operations.api.application.query.IncidentQueryResult;
import com.dnnthanh.marketplace.be.platform.mapping.ModelResponseMapper;
import com.dnnthanh.marketplace.be.platform.mapping.PlatformMapperConfig;
import org.mapstruct.Mapper;

/** Maps operations read models to HTTP views. */
@Mapper(config = PlatformMapperConfig.class)
public interface OperationsApiMapper
        extends ModelResponseMapper<IncidentQueryResult, IncidentView> {
    @Override
    IncidentView modelToResponse(IncidentQueryResult result);
}
