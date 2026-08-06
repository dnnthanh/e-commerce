package com.dnnthanh.marketplace.be.operations.api.api.response;

import java.time.LocalDateTime;

public record IncidentView(
        Long id,
        String type,
        String sourceService,
        String recoveryTarget,
        String aggregateId,
        String status,
        String severity,
        String lastError,
        LocalDateTime firstSeenAt) {}
