package com.dnnthanh.marketplace.be.pricing.api.application.service;

import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import com.dnnthanh.marketplace.be.pricing.api.application.port.in.PriceQuoteUseCase;
import com.dnnthanh.marketplace.be.pricing.api.application.port.out.PriceRuleRepositoryPort;
import com.dnnthanh.marketplace.be.pricing.api.application.query.PriceCandidateCriteria;
import com.dnnthanh.marketplace.be.pricing.api.application.query.PriceQuote;
import com.dnnthanh.marketplace.be.pricing.api.application.query.PriceQuoteQuery;
import com.dnnthanh.marketplace.be.pricing.api.domain.model.PriceRule;
import com.dnnthanh.marketplace.be.pricing.api.domain.model.PriceRule.PriceContext;
import com.dnnthanh.marketplace.be.pricing.api.domain.service.EffectivePriceResolver;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class PriceQuoteServiceImplement implements PriceQuoteUseCase {
    private final PriceRuleRepositoryPort repository;
    private final EffectivePriceResolver resolver;

    @Override
    public PriceQuote quote(PriceQuoteQuery query) {
        var candidateCriteria =
                new PriceCandidateCriteria(
                        Long.valueOf(query.sku()), query.sellerId(), query.channel(), query.at());
        PriceRule rule =
                resolver.resolve(
                        repository.findCandidates(candidateCriteria),
                        new PriceContext(
                                query.sku(), query.sellerId(), query.channel(), query.at()));
        return new PriceQuote(
                UUID.randomUUID().toString(),
                query.sku(),
                query.sellerId(),
                query.channel(),
                rule.currency(),
                rule.amount(),
                rule.ruleId(),
                query.at());
    }
}
