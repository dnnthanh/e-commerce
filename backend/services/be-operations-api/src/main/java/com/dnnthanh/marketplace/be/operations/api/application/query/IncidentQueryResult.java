package com.dnnthanh.marketplace.be.operations.api.application.query;

import java.time.LocalDateTime;

/** Operations incident read model. */
public record IncidentQueryResult(
        Long id,
        String type,
        String sourceService,
        String recoveryTarget,
        String aggregateId,
        String status,
        String severity,
        String lastError,
        LocalDateTime firstSeenAt) {}
