package com.dnnthanh.marketplace.be.fulfillment.api.application.service;

import com.dnnthanh.marketplace.be.fulfillment.api.application.port.out.ShipmentRepositoryPort;
import com.dnnthanh.marketplace.be.fulfillment.api.domain.enumtype.ShipmentStatus;
import com.dnnthanh.marketplace.be.fulfillment.api.domain.exception.InvalidShipmentException;
import com.dnnthanh.marketplace.be.fulfillment.api.domain.model.Shipment;
import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;

/** Fulfillment commands coordinating lifecycle changes and idempotent carrier callbacks. */
@UseCase
@RequiredArgsConstructor
public class ShipmentLifecycleService {
    private final ShipmentRepositoryPort repository;

    public Shipment changeStatus(String shipmentNo, ShipmentStatus next, LocalDateTime occurredAt) {
        Shipment shipment = load(shipmentNo);
        shipment.transitionTo(next, occurredAt);
        return repository.save(shipment);
    }

    /** Seller-scoped lifecycle transition over the canonical aggregate. */
    public Shipment changeStatusForSeller(
            String shipmentNo,
            Long sellerId,
            ShipmentStatus next,
            String carrierCode,
            String trackingNo,
            LocalDateTime occurredAt) {
        Shipment shipment = load(shipmentNo);
        if (!shipment.sellerId().equals(sellerId)) {
            throw new InvalidShipmentException("Shipment does not belong to seller");
        }
        if (carrierCode != null && !carrierCode.isBlank()) {
            shipment.assignCarrier(carrierCode, trackingNo);
        }
        shipment.transitionTo(next, occurredAt);
        return repository.save(shipment);
    }

    public boolean applyCarrierCallback(
            String idempotencyKey,
            String shipmentNo,
            long providerSequence,
            ShipmentStatus next,
            LocalDateTime occurredAt) {
        if (!repository.claimCarrierRequest(idempotencyKey)) {
            return false;
        }
        Shipment shipment = load(shipmentNo);
        boolean changed = shipment.applyCarrierEvent(providerSequence, next, occurredAt);
        if (changed) {
            repository.save(shipment);
        }
        return changed;
    }

    public List<Shipment> byOrder(String orderId) {
        return repository.findByOrderId(orderId);
    }

    private Shipment load(String shipmentNo) {
        return repository
                .findByShipmentNo(shipmentNo)
                .orElseThrow(
                        () -> new InvalidShipmentException("Shipment not found: " + shipmentNo));
    }
}
