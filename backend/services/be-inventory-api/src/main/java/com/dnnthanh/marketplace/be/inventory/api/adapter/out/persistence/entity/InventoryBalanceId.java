package com.dnnthanh.marketplace.be.inventory.api.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class InventoryBalanceId implements Serializable {
    @Column(name = "sku_id")
    private Long skuId;

    @Column(name = "warehouse_id")
    private Long warehouseId;
}
