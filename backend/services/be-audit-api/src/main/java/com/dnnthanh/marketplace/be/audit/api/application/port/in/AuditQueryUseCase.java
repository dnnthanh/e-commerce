package com.dnnthanh.marketplace.be.audit.api.application.port.in;

import com.dnnthanh.marketplace.be.audit.api.application.query.AuditQueryResult;
import com.dnnthanh.marketplace.be.audit.api.application.query.AuditSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Input port for audit read use cases. */
public interface AuditQueryUseCase {
    Page<AuditQueryResult> search(AuditSearchCriteria criteria, Pageable pageable);
}
