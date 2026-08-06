package com.dnnthanh.marketplace.be.pricing.api.adapter.in.web;

import com.dnnthanh.marketplace.be.pricing.api.adapter.in.web.mapper.PricingApiMapper;
import com.dnnthanh.marketplace.be.pricing.api.api.PricingApi;
import com.dnnthanh.marketplace.be.pricing.api.api.PricingInternalApi;
import com.dnnthanh.marketplace.be.pricing.api.api.request.query.PriceQueryRequest;
import com.dnnthanh.marketplace.be.pricing.api.api.request.query.PriceQuoteRequest;
import com.dnnthanh.marketplace.be.pricing.api.api.response.PriceQuoteResponse;
import com.dnnthanh.marketplace.be.pricing.api.api.response.PriceView;
import com.dnnthanh.marketplace.be.pricing.api.application.port.in.PriceQuoteUseCase;
import com.dnnthanh.marketplace.be.pricing.api.application.port.in.PricingUseCase;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PricingController implements PricingApi, PricingInternalApi {
    private final PricingUseCase pricing;
    private final PriceQuoteUseCase quotes;
    private final PricingApiMapper mapper;

    @Override
    public PriceView price(PriceQueryRequest request) {
        return mapper.toView(pricing.price(mapper.toCriteria(request)));
    }

    @Override
    public PriceQuoteResponse quote(PriceQuoteRequest request) {
        return mapper.toResponse(quotes.quote(mapper.toQuery(request, LocalDateTime.now())));
    }
}
