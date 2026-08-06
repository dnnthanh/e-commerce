package com.dnnthanh.marketplace.be.promotion.api.domain.model;

import com.dnnthanh.marketplace.be.promotion.api.domain.enumtype.PromotionBenefitType;
import com.dnnthanh.marketplace.be.promotion.api.domain.exception.InvalidPromotionException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

/** Promotion candidate containing eligibility, stacking and usage constraints. */
public record PromotionCandidate(
        String promotionId,
        PromotionBenefitType benefitType,
        BigDecimal benefitValue,
        BigDecimal minimumSpend,
        Set<Long> sellerIds,
        Set<String> skus,
        String exclusionGroup,
        int priority,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        long globalLimit,
        long perCustomerLimit) {
    public PromotionCandidate {
        Objects.requireNonNull(promotionId);
        Objects.requireNonNull(benefitType);
        Objects.requireNonNull(benefitValue);
        sellerIds = sellerIds == null ? Set.of() : Set.copyOf(sellerIds);
        skus = skus == null ? Set.of() : Set.copyOf(skus);
        if (benefitValue.signum() < 0)
            throw new InvalidPromotionException("Benefit cannot be negative");
        if (globalLimit < 0 || perCustomerLimit < 0)
            throw new InvalidPromotionException("Usage limits cannot be negative");
    }
}
