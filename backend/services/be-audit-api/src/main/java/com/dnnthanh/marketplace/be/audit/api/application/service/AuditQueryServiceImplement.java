package com.dnnthanh.marketplace.be.audit.api.application.service;

import com.dnnthanh.marketplace.be.audit.api.application.port.in.AuditQueryUseCase;
import com.dnnthanh.marketplace.be.audit.api.application.port.out.AuditQueryPort;
import com.dnnthanh.marketplace.be.audit.api.application.query.AuditQueryResult;
import com.dnnthanh.marketplace.be.audit.api.application.query.AuditSearchCriteria;
import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Query-only audit orchestration. */
@UseCase
@RequiredArgsConstructor
public class AuditQueryServiceImplement implements AuditQueryUseCase {
    private final AuditQueryPort auditQueryPort;

    @Override
    public Page<AuditQueryResult> search(AuditSearchCriteria criteria, Pageable pageable) {
        return auditQueryPort.search(criteria, pageable);
    }
}
