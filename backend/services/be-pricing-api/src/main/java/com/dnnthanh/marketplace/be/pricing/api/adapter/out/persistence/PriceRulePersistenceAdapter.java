package com.dnnthanh.marketplace.be.pricing.api.adapter.out.persistence;

import com.dnnthanh.marketplace.be.platform.stereotype.Persistence;
import com.dnnthanh.marketplace.be.pricing.api.adapter.out.persistence.entity.PriceRuleHistoryJpaEntity;
import com.dnnthanh.marketplace.be.pricing.api.adapter.out.persistence.mapper.PriceRulePersistenceMapper;
import com.dnnthanh.marketplace.be.pricing.api.adapter.out.persistence.repository.PriceRuleHistoryJpaRepository;
import com.dnnthanh.marketplace.be.pricing.api.adapter.out.persistence.repository.PriceRuleJpaRepository;
import com.dnnthanh.marketplace.be.pricing.api.application.port.out.PriceRuleRepositoryPort;
import com.dnnthanh.marketplace.be.pricing.api.application.query.PriceCandidateCriteria;
import com.dnnthanh.marketplace.be.pricing.api.domain.model.PriceRule;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Persistence
@RequiredArgsConstructor
public class PriceRulePersistenceAdapter implements PriceRuleRepositoryPort {
    private final PriceRuleJpaRepository rules;
    private final PriceRuleHistoryJpaRepository history;
    private final PriceRulePersistenceMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<PriceRule> findCandidates(PriceCandidateCriteria criteria) {
        return rules.findEffectiveCandidatesNative(criteria).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void appendHistory(String ruleId, String action, String actorId, LocalDateTime at) {
        history.save(new PriceRuleHistoryJpaEntity(Long.valueOf(ruleId), action, actorId, at));
    }
}
