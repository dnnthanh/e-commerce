package com.dnnthanh.marketplace.be.fulfillment.api.application.port.in;

import com.dnnthanh.marketplace.be.fulfillment.api.domain.enumtype.ShipmentStatus;
import java.time.LocalDateTime;
import java.util.List;

/** Inbound shipment query/transition use case. */
public interface FulfillmentUseCase {
    List<ShipmentResult> shipments(String orderId);

    ShipmentResult update(String shipmentNo, UpdateShipmentCommand command);

    record UpdateShipmentCommand(
            Long sellerId, ShipmentStatus status, String carrierCode, String trackingNo) {}

    record ShipmentResult(
            String shipmentNo,
            Long sellerId,
            Long warehouseId,
            String carrierCode,
            String trackingNo,
            ShipmentStatus status,
            LocalDateTime updatedAt) {}
}
