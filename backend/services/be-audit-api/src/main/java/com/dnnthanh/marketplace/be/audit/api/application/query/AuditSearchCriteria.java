package com.dnnthanh.marketplace.be.audit.api.application.query;

import com.dnnthanh.marketplace.be.audit.api.application.exception.InvalidAuditSearchCriteriaException;
import java.time.LocalDateTime;

/** Application-level filters for audit native SQL search. */
public record AuditSearchCriteria(
        String actorId,
        String action,
        String resourceType,
        String resourceId,
        LocalDateTime from,
        LocalDateTime to) {

    public AuditSearchCriteria {
        if (from != null && to != null && !from.isBefore(to)) {
            throw new InvalidAuditSearchCriteriaException("Audit time range is invalid");
        }
    }
}
