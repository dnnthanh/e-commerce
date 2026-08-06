package com.dnnthanh.marketplace.be.fulfillment.api.domain.model;

import com.dnnthanh.marketplace.be.fulfillment.api.domain.enumtype.ShipmentStatus;
import com.dnnthanh.marketplace.be.fulfillment.api.domain.exception.InvalidShipmentException;
import com.dnnthanh.marketplace.be.fulfillment.api.domain.exception.InvalidShipmentTransitionException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Shipment aggregate supporting partial fulfillment, carrier ordering and delivery retry. */
public final class Shipment {
    private final String shipmentNo;
    private final String orderId;
    private final Long sellerId;
    private final Long warehouseId;
    private final List<ShipmentLine> lines;
    private final List<TrackingEvent> tracking;
    private ShipmentStatus status;
    private String carrierCode;
    private String trackingNo;
    private long carrierSequence;

    public Shipment(
            String shipmentNo,
            String orderId,
            Long sellerId,
            Long warehouseId,
            List<ShipmentLine> lines) {
        this.shipmentNo = Objects.requireNonNull(shipmentNo);
        this.orderId = Objects.requireNonNull(orderId);
        this.sellerId = Objects.requireNonNull(sellerId);
        this.warehouseId = Objects.requireNonNull(warehouseId);
        if (lines == null || lines.isEmpty()) {
            throw new InvalidShipmentException("Shipment requires at least one line");
        }
        this.lines = List.copyOf(lines);
        this.tracking = new ArrayList<>();
        this.status = ShipmentStatus.ALLOCATED;
    }

    public static Shipment rehydrate(
            String shipmentNo,
            String orderId,
            Long sellerId,
            Long warehouseId,
            List<ShipmentLine> lines,
            ShipmentStatus status,
            String carrierCode,
            String trackingNo,
            long carrierSequence,
            List<TrackingEvent> tracking) {
        Shipment shipment = new Shipment(shipmentNo, orderId, sellerId, warehouseId, lines);
        shipment.status = Objects.requireNonNull(status);
        shipment.carrierCode = carrierCode;
        shipment.trackingNo = trackingNo;
        shipment.carrierSequence = carrierSequence;
        shipment.tracking.clear();
        shipment.tracking.addAll(tracking == null ? List.of() : tracking);
        return shipment;
    }

    public void transitionTo(ShipmentStatus next, LocalDateTime occurredAt) {
        if (!status.canTransitionTo(next)) {
            throw new InvalidShipmentTransitionException(status, next);
        }
        status = next;
        tracking.add(new TrackingEvent(++carrierSequence, next, occurredAt, "INTERNAL"));
    }

    public void assignCarrier(String carrierCode, String trackingNo) {
        if (status.ordinal() > ShipmentStatus.READY_TO_SHIP.ordinal()) {
            throw new InvalidShipmentException("Carrier cannot be changed after handover");
        }
        this.carrierCode = Objects.requireNonNull(carrierCode);
        this.trackingNo = Objects.requireNonNull(trackingNo);
    }

    /**
     * Accepts provider tracking only when its sequence is newer, making webhook retries harmless.
     */
    public boolean applyCarrierEvent(long sequence, ShipmentStatus next, LocalDateTime occurredAt) {
        if (sequence <= carrierSequence) {
            return false;
        }
        if (!status.canTransitionTo(next)) {
            throw new InvalidShipmentTransitionException(status, next);
        }
        carrierSequence = sequence;
        status = next;
        tracking.add(new TrackingEvent(sequence, next, occurredAt, "CARRIER"));
        return true;
    }

    public String shipmentNo() {
        return shipmentNo;
    }

    public String orderId() {
        return orderId;
    }

    public Long sellerId() {
        return sellerId;
    }

    public Long warehouseId() {
        return warehouseId;
    }

    public List<ShipmentLine> lines() {
        return lines;
    }

    public List<TrackingEvent> tracking() {
        return List.copyOf(tracking);
    }

    public ShipmentStatus status() {
        return status;
    }

    public String carrierCode() {
        return carrierCode;
    }

    public String trackingNo() {
        return trackingNo;
    }

    public long carrierSequence() {
        return carrierSequence;
    }

    /** Immutable order-line quantity assigned to this package. */
    public record ShipmentLine(Long orderLineId, String sku, int quantity) {
        public ShipmentLine {
            Objects.requireNonNull(orderLineId);
            Objects.requireNonNull(sku);
            if (quantity <= 0)
                throw new InvalidShipmentException("Shipment quantity must be positive");
        }
    }

    /** Ordered tracking entry. */
    public record TrackingEvent(
            long sequence, ShipmentStatus status, LocalDateTime occurredAt, String source) {}
}
