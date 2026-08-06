package com.dnnthanh.marketplace.be.settlement.api.adapter.in.web.mapper;

import com.dnnthanh.marketplace.be.platform.mapping.ModelResponseMapper;
import com.dnnthanh.marketplace.be.platform.mapping.PlatformMapperConfig;
import com.dnnthanh.marketplace.be.platform.mapping.RequestModelMapper;
import com.dnnthanh.marketplace.be.settlement.api.api.request.search.SettlementSearchRequest;
import com.dnnthanh.marketplace.be.settlement.api.api.response.SettlementView;
import com.dnnthanh.marketplace.be.settlement.api.application.dto.SettlementResult;
import com.dnnthanh.marketplace.be.settlement.api.application.query.SettlementSearchCriteria;
import org.mapstruct.Mapper;

/** Maps settlement application results to HTTP views. */
@Mapper(config = PlatformMapperConfig.class)
public interface SettlementApiMapper
        extends RequestModelMapper<SettlementSearchRequest, SettlementSearchCriteria>,
                ModelResponseMapper<SettlementResult, SettlementView> {
    @Override
    SettlementSearchCriteria requestToModel(SettlementSearchRequest request);

    @Override
    SettlementView modelToResponse(SettlementResult result);
}
