package com.dnnthanh.marketplace.be.checkout.api.adapter.out.internal.inventory.rest.model;

import java.util.List;

public record InventoryAttachOrderRequest(List<String> reservationKeys, String orderId) {}
