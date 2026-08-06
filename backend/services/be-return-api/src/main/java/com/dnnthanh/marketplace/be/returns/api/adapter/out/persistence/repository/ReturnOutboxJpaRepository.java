package com.dnnthanh.marketplace.be.returns.api.adapter.out.persistence.repository;

import com.dnnthanh.marketplace.be.returns.api.adapter.out.persistence.entity.ReturnOutboxJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data repository for Return outbox events. */
public interface ReturnOutboxJpaRepository extends JpaRepository<ReturnOutboxJpaEntity, Long> {}
