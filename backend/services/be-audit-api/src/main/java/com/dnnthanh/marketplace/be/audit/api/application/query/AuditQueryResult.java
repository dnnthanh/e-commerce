package com.dnnthanh.marketplace.be.audit.api.application.query;

import java.time.LocalDateTime;

/** Audit read-model row returned by the outbound query port. */
public record AuditQueryResult(
        String eventId,
        String actorId,
        String actorType,
        String action,
        String resourceType,
        String resourceId,
        String sourceService,
        String traceId,
        LocalDateTime occurredAt) {}
