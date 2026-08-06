package com.dnnthanh.marketplace.be.audit.api.api.request.search;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Grouped HTTP filters for audit-history search. Pagination is supplied by Spring Pageable. */
@Getter
@Setter
@NoArgsConstructor
public class AuditSearchRequest {
    private String actorId;
    private String action;
    private String resourceType;
    private String resourceId;
    private LocalDateTime from;
    private LocalDateTime to;
}
