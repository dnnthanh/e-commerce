package com.dnnthanh.marketplace.be.promotion.api.adapter.out.persistence.repository;

import com.dnnthanh.marketplace.be.promotion.api.adapter.out.persistence.entity.PromotionJpaEntity;
import com.dnnthanh.marketplace.be.promotion.api.adapter.out.persistence.projection.PromotionCandidateProjection;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Spring Data repository; relational candidate search is deliberately native SQL. */
public interface PromotionJpaRepository extends JpaRepository<PromotionJpaEntity, Long> {

    @Query(
            value =
                    """
          SELECT p.id AS promotionId,
                 p.code AS code,
                 p.promotion_type AS promotionType,
                 p.stacking_group AS stackingGroup,
                 p.priority AS priority,
                 p.start_at AS startAt,
                 p.end_at AS endAt,
                 c.condition_type AS conditionType,
                 c.condition_json::text AS conditionJson
          FROM promotion p
          LEFT JOIN promotion_condition c ON c.promotion_id = p.id
          WHERE p.status = 'ACTIVE'
            AND p.start_at <= :at
            AND p.end_at > :at
          ORDER BY p.priority DESC, p.id, c.id
          """,
            nativeQuery = true)
    List<PromotionCandidateProjection> findActiveCandidatesNative(@Param("at") LocalDateTime at);
}
