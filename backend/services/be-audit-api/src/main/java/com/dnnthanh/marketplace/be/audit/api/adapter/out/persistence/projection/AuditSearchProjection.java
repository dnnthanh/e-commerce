package com.dnnthanh.marketplace.be.audit.api.adapter.out.persistence.projection;

import java.time.LocalDateTime;

/** Native audit-search projection. */
public interface AuditSearchProjection {
    String getEventId();

    String getActorId();

    String getActorType();

    String getAction();

    String getResourceType();

    String getResourceId();

    String getSourceService();

    String getTraceId();

    LocalDateTime getOccurredAt();
}
