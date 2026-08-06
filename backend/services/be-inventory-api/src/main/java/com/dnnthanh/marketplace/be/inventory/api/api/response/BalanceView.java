package com.dnnthanh.marketplace.be.inventory.api.api.response;

public record BalanceView(
        Long skuId, Long warehouseId, long onHand, long reserved, long available, long version) {}
