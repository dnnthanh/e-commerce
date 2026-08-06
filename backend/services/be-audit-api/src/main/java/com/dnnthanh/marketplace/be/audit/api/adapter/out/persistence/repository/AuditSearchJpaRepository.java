package com.dnnthanh.marketplace.be.audit.api.adapter.out.persistence.repository;

import com.dnnthanh.marketplace.be.audit.api.adapter.out.persistence.entity.AuditEventJpaEntity;
import com.dnnthanh.marketplace.be.audit.api.adapter.out.persistence.projection.AuditSearchProjection;
import com.dnnthanh.marketplace.be.audit.api.application.query.AuditSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/** Spring Data native-search repository using grouped SpEL filters and Pageable. */
public interface AuditSearchJpaRepository extends Repository<AuditEventJpaEntity, Long> {

    @Query(
            value =
                    """
          SELECT event_id AS eventId,
                 actor_id AS actorId,
                 actor_type AS actorType,
                 action,
                 resource_type AS resourceType,
                 resource_id AS resourceId,
                 source_service AS sourceService,
                 trace_id AS traceId,
                 occurred_at AS occurredAt
          FROM audit_event
          WHERE (:#{#criteria.actorId} IS NULL OR actor_id = :#{#criteria.actorId})
            AND (:#{#criteria.action} IS NULL OR action = :#{#criteria.action})
            AND (:#{#criteria.resourceType} IS NULL OR resource_type = :#{#criteria.resourceType})
            AND (:#{#criteria.resourceId} IS NULL OR resource_id = :#{#criteria.resourceId})
            AND (:#{#criteria.from} IS NULL OR occurred_at >= :#{#criteria.from})
            AND (:#{#criteria.to} IS NULL OR occurred_at < :#{#criteria.to})
          ORDER BY occurred_at DESC, id DESC
          """,
            countQuery =
                    """
          SELECT COUNT(*)
          FROM audit_event
          WHERE (:#{#criteria.actorId} IS NULL OR actor_id = :#{#criteria.actorId})
            AND (:#{#criteria.action} IS NULL OR action = :#{#criteria.action})
            AND (:#{#criteria.resourceType} IS NULL OR resource_type = :#{#criteria.resourceType})
            AND (:#{#criteria.resourceId} IS NULL OR resource_id = :#{#criteria.resourceId})
            AND (:#{#criteria.from} IS NULL OR occurred_at >= :#{#criteria.from})
            AND (:#{#criteria.to} IS NULL OR occurred_at < :#{#criteria.to})
          """,
            nativeQuery = true)
    Page<AuditSearchProjection> search(
            @Param("criteria") AuditSearchCriteria criteria, Pageable pageable);
}
