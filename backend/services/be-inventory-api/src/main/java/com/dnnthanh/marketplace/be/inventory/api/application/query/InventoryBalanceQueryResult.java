package com.dnnthanh.marketplace.be.inventory.api.application.query;

/**
 * Read-only inventory balance returned to application callers.
 *
 * @param skuId SKU identifier
 * @param warehouseId warehouse identifier
 * @param onHand physical quantity
 * @param reserved active reservation quantity
 * @param available sellable quantity
 * @param version optimistic-lock version
 */
public record InventoryBalanceQueryResult(
        Long skuId, Long warehouseId, long onHand, long reserved, long available, long version) {}
