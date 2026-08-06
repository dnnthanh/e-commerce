package com.dnnthanh.marketplace.be.pricing.api.application.port.out;

import com.dnnthanh.marketplace.be.pricing.api.application.query.PriceCandidateCriteria;
import com.dnnthanh.marketplace.be.pricing.api.domain.model.PriceRule;
import java.time.LocalDateTime;
import java.util.List;

/** Persistence boundary for effective price candidates and price history. */
public interface PriceRuleRepositoryPort {
    List<PriceRule> findCandidates(PriceCandidateCriteria criteria);

    void appendHistory(String ruleId, String action, String actorId, LocalDateTime at);
}
