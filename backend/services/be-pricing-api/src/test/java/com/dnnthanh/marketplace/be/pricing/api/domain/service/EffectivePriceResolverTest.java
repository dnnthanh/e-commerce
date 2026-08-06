package com.dnnthanh.marketplace.be.pricing.api.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dnnthanh.marketplace.be.pricing.api.domain.enumtype.PriceSource;
import com.dnnthanh.marketplace.be.pricing.api.domain.model.PriceRule;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class EffectivePriceResolverTest {
    @Test
    void resolvesHigherPriorityApplicableSellerPrice() {
        LocalDateTime now = LocalDateTime.now();
        var base =
                new PriceRule(
                        "base",
                        "SKU",
                        null,
                        null,
                        "VND",
                        new BigDecimal("100"),
                        PriceSource.BASE,
                        1,
                        null,
                        null);
        var seller =
                new PriceRule(
                        "seller",
                        "SKU",
                        1L,
                        null,
                        "VND",
                        new BigDecimal("90"),
                        PriceSource.SELLER,
                        10,
                        now.minusDays(1),
                        now.plusDays(1));
        var rule =
                new EffectivePriceResolver()
                        .resolve(
                                List.of(base, seller),
                                new PriceRule.PriceContext("SKU", 1L, "WEB", now));
        assertEquals("seller", rule.ruleId());
    }
}
