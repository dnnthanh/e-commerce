package com.dnnthanh.marketplace.be.pricing.api.domain.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dnnthanh.marketplace.be.pricing.api.domain.enumtype.PriceSource;
import com.dnnthanh.marketplace.be.pricing.api.domain.exception.InvalidPriceRuleException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/** Price-rule invariant tests. */
class PriceRuleTest {
    @Test
    void usesHalfOpenValidityWindow() {
        var start = LocalDateTime.of(2026, 8, 1, 0, 0);
        var end = start.plusDays(1);
        var rule =
                new PriceRule(
                        "rule-1",
                        "SKU-1",
                        20L,
                        "WEB",
                        "VND",
                        new BigDecimal("100000"),
                        PriceSource.SELLER,
                        1,
                        start,
                        end);

        assertTrue(rule.applies(contextAt(start)));
        assertTrue(rule.applies(contextAt(end.minusNanos(1))));
        assertFalse(rule.applies(contextAt(end)));
    }

    @Test
    void rejectsNegativeAmount() {
        assertThrows(
                InvalidPriceRuleException.class,
                () ->
                        new PriceRule(
                                "rule-1",
                                "SKU-1",
                                null,
                                null,
                                "VND",
                                new BigDecimal("-1"),
                                PriceSource.BASE,
                                1,
                                LocalDateTime.now(),
                                null));
    }

    private PriceRule.PriceContext contextAt(LocalDateTime at) {
        return new PriceRule.PriceContext("SKU-1", 20L, "WEB", at);
    }
}
