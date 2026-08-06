package com.dnnthanh.marketplace.be.checkout.api.adapter.out.internal.inventory.rest.model;

public record InventoryReservationRequest(
        String reservationKey, Long skuId, Long warehouseId, int quantity, int ttlMinutes) {}
