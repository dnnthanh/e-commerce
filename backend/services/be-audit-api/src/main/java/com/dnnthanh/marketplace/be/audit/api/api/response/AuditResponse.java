package com.dnnthanh.marketplace.be.audit.api.api.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;

/** Audit event returned by the HTTP query contract. */
@Value
@Builder
public class AuditResponse {
    String eventId;
    String actorId;
    String actorType;
    String action;
    String resourceType;
    String resourceId;
    String sourceService;
    String traceId;
    LocalDateTime occurredAt;
}
