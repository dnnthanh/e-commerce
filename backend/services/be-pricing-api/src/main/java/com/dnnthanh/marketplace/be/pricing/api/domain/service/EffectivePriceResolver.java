package com.dnnthanh.marketplace.be.pricing.api.domain.service;

import com.dnnthanh.marketplace.be.pricing.api.domain.exception.EffectivePriceNotFoundException;
import com.dnnthanh.marketplace.be.pricing.api.domain.model.PriceRule;
import com.dnnthanh.marketplace.be.pricing.api.domain.model.PriceRule.PriceContext;
import java.util.Comparator;
import java.util.List;

/** Deterministically resolves overlapping price rules by priority then specificity. */
public final class EffectivePriceResolver {
    public PriceRule resolve(List<PriceRule> rules, PriceContext context) {
        return rules.stream()
                .filter(rule -> rule.applies(context))
                .max(
                        Comparator.comparingInt(PriceRule::priority)
                                .thenComparingInt(this::specificity)
                                .thenComparing(PriceRule::ruleId))
                .orElseThrow(() -> new EffectivePriceNotFoundException(context.sku()));
    }

    private int specificity(PriceRule rule) {
        int result = 0;
        if (rule.sellerId() != null) result += 2;
        if (rule.channel() != null) result += 1;
        return result;
    }
}
