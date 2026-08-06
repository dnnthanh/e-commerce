package com.dnnthanh.marketplace.be.pricing.api.adapter.in.web.mapper;

import com.dnnthanh.marketplace.be.platform.mapping.MapperContract;
import com.dnnthanh.marketplace.be.platform.mapping.PlatformMapperConfig;
import com.dnnthanh.marketplace.be.pricing.api.api.request.query.PriceQueryRequest;
import com.dnnthanh.marketplace.be.pricing.api.api.request.query.PriceQuoteRequest;
import com.dnnthanh.marketplace.be.pricing.api.api.response.PriceQuoteResponse;
import com.dnnthanh.marketplace.be.pricing.api.api.response.PriceView;
import com.dnnthanh.marketplace.be.pricing.api.application.query.PriceQueryCriteria;
import com.dnnthanh.marketplace.be.pricing.api.application.query.PriceQuote;
import com.dnnthanh.marketplace.be.pricing.api.application.query.PriceQuoteQuery;
import com.dnnthanh.marketplace.be.pricing.api.application.query.PriceResult;
import java.time.LocalDateTime;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = PlatformMapperConfig.class)
public interface PricingApiMapper extends MapperContract {
    PriceQueryCriteria toCriteria(PriceQueryRequest request);

    @Mapping(target = "at", expression = "java(now)")
    PriceQuoteQuery toQuery(PriceQuoteRequest request, LocalDateTime now);

    PriceView toView(PriceResult result);

    PriceQuoteResponse toResponse(PriceQuote quote);
}
