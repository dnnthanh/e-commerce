package com.dnnthanh.marketplace.be.promotion.api.domain.service;

import com.dnnthanh.marketplace.be.promotion.api.domain.model.PromotionCandidate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Evaluates eligibility and stacking while explaining every applied/rejected rule. */
public final class PromotionEngine {
    public Evaluation evaluate(List<PromotionCandidate> candidates, PromotionContext context) {
        List<Decision> decisions = new ArrayList<>();
        Set<String> consumedGroups = new HashSet<>();
        BigDecimal remaining = context.subtotal();

        var sorted =
                candidates.stream()
                        .sorted(
                                Comparator.comparingInt(PromotionCandidate::priority)
                                        .reversed()
                                        .thenComparing(PromotionCandidate::promotionId))
                        .toList();

        for (PromotionCandidate candidate : sorted) {
            String rejection = rejectionReason(candidate, context, consumedGroups);
            if (rejection != null) {
                decisions.add(
                        new Decision(candidate.promotionId(), false, BigDecimal.ZERO, rejection));
                continue;
            }
            BigDecimal discount =
                    calculate(candidate, remaining)
                            .min(remaining)
                            .setScale(2, RoundingMode.HALF_UP);
            remaining = remaining.subtract(discount);
            if (candidate.exclusionGroup() != null) consumedGroups.add(candidate.exclusionGroup());
            decisions.add(new Decision(candidate.promotionId(), true, discount, "APPLIED"));
        }
        return new Evaluation(context.subtotal().subtract(remaining), List.copyOf(decisions));
    }

    private String rejectionReason(
            PromotionCandidate candidate, PromotionContext context, Set<String> consumedGroups) {
        LocalDateTime at = context.at();
        if (candidate.startsAt() != null && at.isBefore(candidate.startsAt())) return "NOT_STARTED";
        if (candidate.endsAt() != null && !at.isBefore(candidate.endsAt())) return "ENDED";
        if (candidate.minimumSpend() != null
                && context.subtotal().compareTo(candidate.minimumSpend()) < 0)
            return "MINIMUM_SPEND";
        if (!candidate.sellerIds().isEmpty() && !candidate.sellerIds().contains(context.sellerId()))
            return "SELLER_NOT_ELIGIBLE";
        if (!candidate.skus().isEmpty()
                && context.skus().stream().noneMatch(candidate.skus()::contains))
            return "SKU_NOT_ELIGIBLE";
        if (candidate.globalLimit() > 0
                && context.globalUsage(candidate.promotionId()) >= candidate.globalLimit())
            return "GLOBAL_LIMIT";
        if (candidate.perCustomerLimit() > 0
                && context.customerUsage(candidate.promotionId()) >= candidate.perCustomerLimit())
            return "CUSTOMER_LIMIT";
        if (candidate.exclusionGroup() != null
                && consumedGroups.contains(candidate.exclusionGroup())) return "STACKING_EXCLUDED";
        return null;
    }

    private BigDecimal calculate(PromotionCandidate candidate, BigDecimal amount) {
        return switch (candidate.benefitType()) {
            case FIXED_AMOUNT, SHIPPING_DISCOUNT -> candidate.benefitValue();
            case PERCENTAGE ->
                    amount.multiply(candidate.benefitValue())
                            .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        };
    }

    public interface PromotionContext {
        BigDecimal subtotal();

        Long sellerId();

        Set<String> skus();

        LocalDateTime at();

        long globalUsage(String promotionId);

        long customerUsage(String promotionId);
    }

    public record Decision(
            String promotionId, boolean applied, BigDecimal discount, String reason) {}

    public record Evaluation(BigDecimal totalDiscount, List<Decision> decisions) {}
}
