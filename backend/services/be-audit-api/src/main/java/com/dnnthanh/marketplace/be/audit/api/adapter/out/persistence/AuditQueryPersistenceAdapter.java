package com.dnnthanh.marketplace.be.audit.api.adapter.out.persistence;

import com.dnnthanh.marketplace.be.audit.api.adapter.out.persistence.mapper.AuditPersistenceMapper;
import com.dnnthanh.marketplace.be.audit.api.adapter.out.persistence.repository.AuditSearchJpaRepository;
import com.dnnthanh.marketplace.be.audit.api.application.port.out.AuditQueryPort;
import com.dnnthanh.marketplace.be.audit.api.application.query.AuditQueryResult;
import com.dnnthanh.marketplace.be.audit.api.application.query.AuditSearchCriteria;
import com.dnnthanh.marketplace.be.platform.stereotype.Persistence;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Native-SQL audit search adapter implemented through Spring Data JPA. */
@Persistence
@RequiredArgsConstructor
public class AuditQueryPersistenceAdapter implements AuditQueryPort {
    private final AuditSearchJpaRepository auditSearchJpaRepository;
    private final AuditPersistenceMapper auditPersistenceMapper;

    @Override
    public Page<AuditQueryResult> search(AuditSearchCriteria criteria, Pageable pageable) {
        return auditSearchJpaRepository
                .search(criteria, pageable)
                .map(auditPersistenceMapper::toResult);
    }
}
