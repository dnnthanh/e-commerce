package com.dnnthanh.marketplace.be.pricing.api.domain.model;

import com.dnnthanh.marketplace.be.pricing.api.domain.enumtype.PriceSource;
import com.dnnthanh.marketplace.be.pricing.api.domain.exception.InvalidPriceRuleException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;

/** One immutable price rule with precedence and validity window. */
public record PriceRule(
        String ruleId,
        String sku,
        Long sellerId,
        String channel,
        String currency,
        BigDecimal amount,
        PriceSource source,
        int priority,
        LocalDateTime validFrom,
        LocalDateTime validTo) {
    public PriceRule {
        Objects.requireNonNull(ruleId);
        Objects.requireNonNull(sku);
        Objects.requireNonNull(currency);
        Objects.requireNonNull(amount);
        Objects.requireNonNull(source);
        if (amount.signum() < 0) throw new InvalidPriceRuleException("Price cannot be negative");
        amount = amount.setScale(2, RoundingMode.HALF_UP);
        if (validFrom != null && validTo != null && !validFrom.isBefore(validTo)) {
            throw new InvalidPriceRuleException("Price validity window is invalid");
        }
    }

    public boolean applies(PriceContext context) {
        if (!sku.equals(context.sku())) return false;
        if (sellerId != null && !sellerId.equals(context.sellerId())) return false;
        if (channel != null && !channel.equals(context.channel())) return false;
        if (validFrom != null && context.at().isBefore(validFrom)) return false;
        return validTo == null || context.at().isBefore(validTo);
    }

    /** Effective-price lookup context. */
    public record PriceContext(String sku, Long sellerId, String channel, LocalDateTime at) {}
}
