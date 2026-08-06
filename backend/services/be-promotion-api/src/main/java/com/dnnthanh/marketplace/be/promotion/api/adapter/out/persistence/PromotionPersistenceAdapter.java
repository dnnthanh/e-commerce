package com.dnnthanh.marketplace.be.promotion.api.adapter.out.persistence;

import com.dnnthanh.marketplace.be.platform.stereotype.Persistence;
import com.dnnthanh.marketplace.be.promotion.api.adapter.out.persistence.exception.PromotionPersistenceException;
import com.dnnthanh.marketplace.be.promotion.api.adapter.out.persistence.projection.PromotionCandidateProjection;
import com.dnnthanh.marketplace.be.promotion.api.adapter.out.persistence.repository.PromotionJpaRepository;
import com.dnnthanh.marketplace.be.promotion.api.application.port.out.PromotionRepositoryPort;
import com.dnnthanh.marketplace.be.promotion.api.domain.model.Promotion;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** Spring Data + native-SQL promotion candidate adapter. */
@Persistence
@RequiredArgsConstructor
public class PromotionPersistenceAdapter implements PromotionRepositoryPort {
    private static final String BENEFIT_CONDITION = "BENEFIT";
    private static final String MIN_SPEND_CONDITION = "MIN_SPEND";
    private static final String GLOBAL_LIMIT_CONDITION = "GLOBAL_LIMIT";
    private static final String CUSTOMER_LIMIT_CONDITION = "CUSTOMER_LIMIT";
    private static final String SELLER_IDS_CONDITION = "SELLER_IDS";
    private static final String SKU_IDS_CONDITION = "SKU_IDS";
    private static final String CATEGORY_IDS_CONDITION = "CATEGORY_IDS";
    private static final String CHANNELS_CONDITION = "CHANNELS";
    private static final String CUSTOMER_SEGMENTS_CONDITION = "CUSTOMER_SEGMENTS";

    private final PromotionJpaRepository repository;
    private final ObjectMapper json;

    @Override
    public List<Promotion> findActive(LocalDateTime at) {
        Map<Long, CandidateBuilder> candidates = new LinkedHashMap<>();
        for (PromotionCandidateProjection row : repository.findActiveCandidatesNative(at)) {
            CandidateBuilder candidate =
                    candidates.computeIfAbsent(
                            row.getPromotionId(), ignored -> new CandidateBuilder(row));
            candidate.accept(row.getConditionType(), row.getConditionJson());
        }
        return candidates.values().stream().map(CandidateBuilder::build).toList();
    }

    private final class CandidateBuilder {
        private final long id;
        private final String code;
        private final Promotion.Type type;
        private final String stackingGroup;
        private final int priority;
        private final LocalDateTime startAt;
        private final LocalDateTime endAt;
        private BigDecimal benefit = BigDecimal.ZERO;
        private BigDecimal minimumSpend = BigDecimal.ZERO;
        private long globalLimit;
        private long customerLimit;
        private final Set<Long> sellerIds = new HashSet<>();
        private final Set<String> skuIds = new HashSet<>();
        private final Set<Long> categoryIds = new HashSet<>();
        private final Set<String> channels = new HashSet<>();
        private final Set<String> customerSegments = new HashSet<>();

        private CandidateBuilder(PromotionCandidateProjection row) {
            id = row.getPromotionId();
            code = row.getCode();
            type = Promotion.Type.valueOf(row.getPromotionType());
            stackingGroup = row.getStackingGroup();
            priority = row.getPriority();
            startAt = row.getStartAt();
            endAt = row.getEndAt();
        }

        private void accept(String conditionType, String conditionJson) {
            if (conditionType == null || conditionJson == null) {
                return;
            }
            try {
                JsonNode node = json.readTree(conditionJson);
                switch (conditionType) {
                    case BENEFIT_CONDITION -> benefit = node.path("value").decimalValue();
                    case MIN_SPEND_CONDITION -> minimumSpend = node.path("amount").decimalValue();
                    case GLOBAL_LIMIT_CONDITION -> globalLimit = node.path("count").longValue();
                    case CUSTOMER_LIMIT_CONDITION -> customerLimit = node.path("count").longValue();
                    case SELLER_IDS_CONDITION ->
                            node.path("values").forEach(value -> sellerIds.add(value.longValue()));
                    case SKU_IDS_CONDITION ->
                            node.path("values").forEach(value -> skuIds.add(value.asText()));
                    case CATEGORY_IDS_CONDITION ->
                            node.path("values")
                                    .forEach(value -> categoryIds.add(value.longValue()));
                    case CHANNELS_CONDITION ->
                            node.path("values").forEach(value -> channels.add(value.asText()));
                    case CUSTOMER_SEGMENTS_CONDITION ->
                            node.path("values")
                                    .forEach(value -> customerSegments.add(value.asText()));
                    default -> {
                        // Eligibility policies that are not yet modeled are ignored explicitly.
                    }
                }
            } catch (Exception failure) {
                throw new PromotionPersistenceException(
                        "Invalid promotion condition for " + id, failure);
            }
        }

        private Promotion build() {
            return new Promotion(
                    id,
                    code,
                    type,
                    stackingGroup,
                    priority,
                    startAt,
                    endAt,
                    benefit,
                    minimumSpend,
                    globalLimit,
                    customerLimit,
                    Set.copyOf(sellerIds),
                    Set.copyOf(skuIds),
                    Set.copyOf(categoryIds),
                    Set.copyOf(channels),
                    Set.copyOf(customerSegments));
        }
    }
}
