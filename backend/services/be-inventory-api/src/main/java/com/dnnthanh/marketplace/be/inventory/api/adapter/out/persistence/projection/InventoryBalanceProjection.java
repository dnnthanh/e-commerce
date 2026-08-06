package com.dnnthanh.marketplace.be.inventory.api.adapter.out.persistence.projection;

public interface InventoryBalanceProjection {
    Long getSkuId();

    Long getWarehouseId();

    long getOnHand();

    long getReserved();

    long getAvailable();

    long getVersion();
}
