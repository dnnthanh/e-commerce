package com.dnnthanh.marketplace.be.promotion.api.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Promotion math tests. */
class PromotionTest {
    @Test
    void percentageNeverExceedsSubtotal() {
        var now = LocalDateTime.now();
        var promotion =
                new Promotion(
                        1L,
                        "P200",
                        Promotion.Type.PERCENTAGE,
                        "G",
                        1,
                        now.minusDays(1),
                        now.plusDays(1),
                        new BigDecimal("200"),
                        BigDecimal.ZERO,
                        0,
                        0);

        assertEquals(new BigDecimal("100.00"), promotion.discount(new BigDecimal("100.00")));
    }

    @Test
    void minimumSpendControlsEligibility() {
        var now = LocalDateTime.now();
        var promotion =
                new Promotion(
                        1L,
                        "F50",
                        Promotion.Type.FIXED_AMOUNT,
                        null,
                        1,
                        now.minusDays(1),
                        now.plusDays(1),
                        new BigDecimal("50"),
                        new BigDecimal("500"),
                        0,
                        0);

        var context =
                new Promotion.PromotionEvaluationContext(
                        new BigDecimal("499"), List.of(), "WEB", "DEFAULT", now);

        assertFalse(promotion.eligible(context));
    }
}
