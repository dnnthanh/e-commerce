package com.dnnthanh.marketplace.be.inventory.api.application.query;

/** Application filters for relational inventory-balance search. */
public record InventoryBalanceSearchCriteria(Long skuId, Long warehouseId) {}
