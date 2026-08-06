package com.dnnthanh.marketplace.be.audit.api.adapter.in.web.mapper;

import com.dnnthanh.marketplace.be.audit.api.api.request.search.AuditSearchRequest;
import com.dnnthanh.marketplace.be.audit.api.api.response.AuditResponse;
import com.dnnthanh.marketplace.be.audit.api.application.query.AuditQueryResult;
import com.dnnthanh.marketplace.be.audit.api.application.query.AuditSearchCriteria;
import com.dnnthanh.marketplace.be.platform.mapping.ModelResponseMapper;
import com.dnnthanh.marketplace.be.platform.mapping.PlatformMapperConfig;
import com.dnnthanh.marketplace.be.platform.mapping.RequestModelMapper;
import org.mapstruct.Mapper;

/** Maps Audit transport contracts to/from application query models. */
@Mapper(config = PlatformMapperConfig.class)
public interface AuditApiMapper
        extends RequestModelMapper<AuditSearchRequest, AuditSearchCriteria>,
                ModelResponseMapper<AuditQueryResult, AuditResponse> {
    @Override
    AuditSearchCriteria requestToModel(AuditSearchRequest request);

    @Override
    AuditResponse modelToResponse(AuditQueryResult result);
}
