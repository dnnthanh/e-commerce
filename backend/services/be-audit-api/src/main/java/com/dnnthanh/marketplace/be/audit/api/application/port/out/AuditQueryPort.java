package com.dnnthanh.marketplace.be.audit.api.application.port.out;

import com.dnnthanh.marketplace.be.audit.api.application.query.AuditQueryResult;
import com.dnnthanh.marketplace.be.audit.api.application.query.AuditSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Read-only audit persistence port. */
public interface AuditQueryPort {
    Page<AuditQueryResult> search(AuditSearchCriteria criteria, Pageable pageable);
}
