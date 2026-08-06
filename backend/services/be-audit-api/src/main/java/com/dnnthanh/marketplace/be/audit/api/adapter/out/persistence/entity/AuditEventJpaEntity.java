package com.dnnthanh.marketplace.be.audit.api.adapter.out.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

/** Minimal JPA metadata for native audit read projections. */
@Getter
@Entity
@Table(name = "audit_event")
public class AuditEventJpaEntity {
    @Id private Long id;
}
