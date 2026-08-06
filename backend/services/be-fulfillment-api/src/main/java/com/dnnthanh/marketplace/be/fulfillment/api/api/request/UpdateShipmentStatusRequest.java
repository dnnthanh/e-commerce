package com.dnnthanh.marketplace.be.fulfillment.api.api.request;

import com.dnnthanh.marketplace.be.fulfillment.api.domain.enumtype.ShipmentStatus;

public record UpdateShipmentStatusRequest(
        Long sellerId, ShipmentStatus status, String carrierCode, String trackingNo) {}
