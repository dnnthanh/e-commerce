package com.dnnthanh.marketplace.be.promotion.api.adapter.out.persistence.projection;

import java.time.LocalDateTime;

/** Flat native-SQL row used to assemble active promotion candidates without N+1 queries. */
public interface PromotionCandidateProjection {
    Long getPromotionId();

    String getCode();

    String getPromotionType();

    String getStackingGroup();

    Integer getPriority();

    LocalDateTime getStartAt();

    LocalDateTime getEndAt();

    String getConditionType();

    String getConditionJson();
}
