package com.dnnthanh.marketplace.be.authorization.api.adapter.out.persistence.repository;

import com.dnnthanh.marketplace.be.authorization.api.adapter.out.persistence.entity.AuthorizationOutboxJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data repository for simple transactional outbox inserts. */
public interface AuthorizationOutboxJpaRepository
        extends JpaRepository<AuthorizationOutboxJpaEntity, Long> {}
