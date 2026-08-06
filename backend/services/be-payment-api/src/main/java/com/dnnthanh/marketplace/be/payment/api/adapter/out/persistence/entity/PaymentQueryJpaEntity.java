package com.dnnthanh.marketplace.be.payment.api.adapter.out.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Minimal entity anchor for Spring Data native payment read queries. */
@Entity
@Table(name = "payment")
public class PaymentQueryJpaEntity {
    @Id private Long id;

    protected PaymentQueryJpaEntity() {}
}
