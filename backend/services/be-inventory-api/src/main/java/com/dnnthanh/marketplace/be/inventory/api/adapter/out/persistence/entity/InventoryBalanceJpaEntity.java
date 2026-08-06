package com.dnnthanh.marketplace.be.inventory.api.adapter.out.persistence.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** Minimal JPA mapping used by Spring Data native inventory read queries. */
@Entity
@Table(name = "inventory_balance")
public class InventoryBalanceJpaEntity {
    @EmbeddedId private InventoryBalanceId id;

    protected InventoryBalanceJpaEntity() {}
}
