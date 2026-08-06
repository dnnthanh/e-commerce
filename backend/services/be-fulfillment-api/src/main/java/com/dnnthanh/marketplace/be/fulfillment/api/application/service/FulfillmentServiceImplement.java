package com.dnnthanh.marketplace.be.fulfillment.api.application.service;

import com.dnnthanh.marketplace.be.fulfillment.api.application.port.in.FulfillmentUseCase;
import com.dnnthanh.marketplace.be.fulfillment.api.application.port.in.FulfillmentUseCase.ShipmentResult;
import com.dnnthanh.marketplace.be.fulfillment.api.application.port.in.FulfillmentUseCase.UpdateShipmentCommand;
import com.dnnthanh.marketplace.be.fulfillment.api.domain.model.Shipment;
import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;

/** Fulfillment application orchestration over the canonical Shipment aggregate. */
@UseCase
@RequiredArgsConstructor
public class FulfillmentServiceImplement implements FulfillmentUseCase {
    private final ShipmentLifecycleService lifecycle;

    @Override
    public List<ShipmentResult> shipments(String orderId) {
        return lifecycle.byOrder(orderId).stream().map(this::toResult).toList();
    }

    @Override
    public ShipmentResult update(String shipmentNo, UpdateShipmentCommand command) {
        Shipment updated =
                lifecycle.changeStatusForSeller(
                        shipmentNo,
                        command.sellerId(),
                        command.status(),
                        command.carrierCode(),
                        command.trackingNo(),
                        LocalDateTime.now());
        return toResult(updated);
    }

    private ShipmentResult toResult(Shipment shipment) {
        LocalDateTime updatedAt =
                shipment.tracking().isEmpty()
                        ? null
                        : shipment.tracking().get(shipment.tracking().size() - 1).occurredAt();
        return new ShipmentResult(
                shipment.shipmentNo(),
                shipment.sellerId(),
                shipment.warehouseId(),
                shipment.carrierCode(),
                shipment.trackingNo(),
                shipment.status(),
                updatedAt);
    }
}
