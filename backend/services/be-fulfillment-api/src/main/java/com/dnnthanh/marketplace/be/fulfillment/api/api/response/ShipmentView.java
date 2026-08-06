package com.dnnthanh.marketplace.be.fulfillment.api.api.response;

import com.dnnthanh.marketplace.be.fulfillment.api.domain.enumtype.ShipmentStatus;
import java.time.LocalDateTime;

public record ShipmentView(
        String shipmentNo,
        Long sellerId,
        Long warehouseId,
        String carrierCode,
        String trackingNo,
        ShipmentStatus status,
        LocalDateTime updatedAt) {}
