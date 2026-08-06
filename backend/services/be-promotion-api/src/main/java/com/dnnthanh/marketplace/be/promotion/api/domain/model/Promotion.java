package com.dnnthanh.marketplace.be.promotion.api.domain.model;

import com.dnnthanh.marketplace.be.platform.model.CodeEnum;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Promotion rule with targeting, stacking and usage-limit metadata. */
public final class Promotion {
    private final Long id;
    private final String code;
    private final Type type;
    private final String stackingGroup;
    private final int priority;
    private final LocalDateTime startAt;
    private final LocalDateTime endAt;
    private final BigDecimal value;
    private final BigDecimal minimumSpend;
    private final long globalLimit;
    private final long perCustomerLimit;
    private final Set<Long> sellerIds;
    private final Set<String> skuIds;
    private final Set<Long> categoryIds;
    private final Set<String> channels;
    private final Set<String> customerSegments;

    public enum Type implements CodeEnum {
        FIXED_AMOUNT,
        PERCENTAGE
    }

    public Promotion(
            Long id,
            String code,
            Type type,
            String stackingGroup,
            int priority,
            LocalDateTime startAt,
            LocalDateTime endAt,
            BigDecimal value,
            BigDecimal minimumSpend,
            long globalLimit,
            long perCustomerLimit) {
        this(
                id,
                code,
                type,
                stackingGroup,
                priority,
                startAt,
                endAt,
                value,
                minimumSpend,
                globalLimit,
                perCustomerLimit,
                Set.of(),
                Set.of(),
                Set.of(),
                Set.of(),
                Set.of());
    }

    public Promotion(
            Long id,
            String code,
            Type type,
            String stackingGroup,
            int priority,
            LocalDateTime startAt,
            LocalDateTime endAt,
            BigDecimal value,
            BigDecimal minimumSpend,
            long globalLimit,
            long perCustomerLimit,
            Set<Long> sellerIds,
            Set<String> skuIds,
            Set<Long> categoryIds,
            Set<String> channels,
            Set<String> customerSegments) {
        this.id = id;
        this.code = Objects.requireNonNull(code);
        this.type = Objects.requireNonNull(type);
        this.stackingGroup = stackingGroup;
        this.priority = priority;
        this.startAt = Objects.requireNonNull(startAt);
        this.endAt = Objects.requireNonNull(endAt);
        this.value = Objects.requireNonNull(value);
        this.minimumSpend = minimumSpend == null ? BigDecimal.ZERO : minimumSpend;
        this.globalLimit = globalLimit;
        this.perCustomerLimit = perCustomerLimit;
        this.sellerIds = immutable(sellerIds);
        this.skuIds = immutable(skuIds);
        this.categoryIds = immutable(categoryIds);
        this.channels = immutable(channels);
        this.customerSegments = immutable(customerSegments);
    }

    public boolean eligible(PromotionEvaluationContext context) {
        if (context.at().isBefore(startAt) || !context.at().isBefore(endAt)) return false;
        if (!channels.isEmpty() && !channels.contains(context.channel())) return false;
        if (!customerSegments.isEmpty() && !customerSegments.contains(context.customerSegment()))
            return false;
        BigDecimal base = eligibleSubtotal(context.lines(), context.subtotal());
        return base.compareTo(minimumSpend) >= 0 && base.signum() > 0;
    }

    /** Returns subtotal affected by target selectors, not always the entire marketplace order. */
    public BigDecimal eligibleSubtotal(List<PromotionLine> lines, BigDecimal fallbackSubtotal) {
        if (sellerIds.isEmpty() && skuIds.isEmpty() && categoryIds.isEmpty()) {
            return fallbackSubtotal == null ? BigDecimal.ZERO : fallbackSubtotal;
        }
        if (lines == null || lines.isEmpty()) return BigDecimal.ZERO;
        return lines.stream()
                .filter(this::matchesLine)
                .map(PromotionLine::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean matchesLine(PromotionLine line) {
        return (sellerIds.isEmpty() || sellerIds.contains(line.sellerId()))
                && (skuIds.isEmpty() || skuIds.contains(line.skuId()))
                && (categoryIds.isEmpty() || categoryIds.contains(line.categoryId()));
    }

    public BigDecimal discount(BigDecimal eligibleSubtotal) {
        BigDecimal raw =
                type == Type.PERCENTAGE ? eligibleSubtotal.multiply(value).movePointLeft(2) : value;
        return raw.min(eligibleSubtotal).max(BigDecimal.ZERO);
    }

    private static <T> Set<T> immutable(Set<T> values) {
        return values == null ? Set.of() : Set.copyOf(values);
    }

    public Long id() {
        return id;
    }

    public String code() {
        return code;
    }

    public String stackingGroup() {
        return stackingGroup;
    }

    public int priority() {
        return priority;
    }

    public long globalLimit() {
        return globalLimit;
    }

    public long perCustomerLimit() {
        return perCustomerLimit;
    }

    public Set<Long> sellerIds() {
        return sellerIds;
    }

    public Set<String> skuIds() {
        return skuIds;
    }

    public Set<Long> categoryIds() {
        return categoryIds;
    }

    public Set<String> channels() {
        return channels;
    }

    public Set<String> customerSegments() {
        return customerSegments;
    }

    public record PromotionLine(Long sellerId, String skuId, Long categoryId, BigDecimal subtotal) {
        public PromotionLine {
            subtotal = subtotal == null ? BigDecimal.ZERO : subtotal;
        }
    }

    public record PromotionEvaluationContext(
            BigDecimal subtotal,
            List<PromotionLine> lines,
            String channel,
            String customerSegment,
            LocalDateTime at) {
        public PromotionEvaluationContext {
            subtotal = subtotal == null ? BigDecimal.ZERO : subtotal;
            lines = lines == null ? List.of() : List.copyOf(lines);
            channel = channel == null ? "WEB" : channel;
            customerSegment = customerSegment == null ? "DEFAULT" : customerSegment;
            at = at == null ? LocalDateTime.now() : at;
        }
    }
}
