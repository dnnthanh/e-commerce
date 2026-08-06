package com.dnnthanh.marketplace.be.promotion.api.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dnnthanh.marketplace.be.promotion.api.domain.enumtype.PromotionBenefitType;
import com.dnnthanh.marketplace.be.promotion.api.domain.model.PromotionCandidate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class PromotionEngineTest {
    @Test
    void rejectsLowerPriorityPromotionInSameExclusionGroup() {
        var high =
                new PromotionCandidate(
                        "A",
                        PromotionBenefitType.PERCENTAGE,
                        new BigDecimal("10"),
                        BigDecimal.ZERO,
                        Set.of(),
                        Set.of(),
                        "G",
                        10,
                        null,
                        null,
                        0,
                        0);
        var low =
                new PromotionCandidate(
                        "B",
                        PromotionBenefitType.FIXED_AMOUNT,
                        new BigDecimal("30"),
                        BigDecimal.ZERO,
                        Set.of(),
                        Set.of(),
                        "G",
                        1,
                        null,
                        null,
                        0,
                        0);
        PromotionEngine.PromotionContext context =
                new PromotionEngine.PromotionContext() {
                    public BigDecimal subtotal() {
                        return new BigDecimal("200");
                    }

                    public Long sellerId() {
                        return 1L;
                    }

                    public Set<String> skus() {
                        return Set.of("SKU");
                    }

                    public LocalDateTime at() {
                        return LocalDateTime.now();
                    }

                    public long globalUsage(String id) {
                        return 0;
                    }

                    public long customerUsage(String id) {
                        return 0;
                    }
                };
        var result = new PromotionEngine().evaluate(List.of(low, high), context);
        assertEquals(
                1, result.decisions().stream().filter(PromotionEngine.Decision::applied).count());
        assertEquals(
                "STACKING_EXCLUDED",
                result.decisions().stream()
                        .filter(d -> !d.applied())
                        .findFirst()
                        .orElseThrow()
                        .reason());
    }
}
