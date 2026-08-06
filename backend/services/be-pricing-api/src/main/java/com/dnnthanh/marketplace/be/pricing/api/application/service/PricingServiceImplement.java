package com.dnnthanh.marketplace.be.pricing.api.application.service;

import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import com.dnnthanh.marketplace.be.pricing.api.application.port.in.PriceQuoteUseCase;
import com.dnnthanh.marketplace.be.pricing.api.application.port.in.PricingUseCase;
import com.dnnthanh.marketplace.be.pricing.api.application.query.PriceQueryCriteria;
import com.dnnthanh.marketplace.be.pricing.api.application.query.PriceQuoteQuery;
import com.dnnthanh.marketplace.be.pricing.api.application.query.PriceResult;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;

/** Application orchestration around immutable effective-price quotes. */
@UseCase
@RequiredArgsConstructor
public class PricingServiceImplement implements PricingUseCase {
    private final PriceQuoteUseCase quotes;

    @Override
    public PriceResult price(PriceQueryCriteria criteria) {
        LocalDateTime resolvedAt = criteria.at() == null ? LocalDateTime.now() : criteria.at();
        String channel = criteria.channel() == null ? "WEB" : criteria.channel();
        var quote =
                quotes.quote(
                        new PriceQuoteQuery(
                                String.valueOf(criteria.skuId()),
                                criteria.sellerId(),
                                channel,
                                resolvedAt));
        return new PriceResult(
                criteria.skuId(),
                criteria.sellerId(),
                quote.amount(),
                quote.currency(),
                Long.valueOf(quote.sourceRuleId()),
                resolvedAt);
    }
}
