package com.dnnthanh.marketplace.be.fulfillment.api.application.port.out;

import com.dnnthanh.marketplace.be.fulfillment.api.domain.model.Shipment;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Persistence boundary for shipment aggregates and atomic carrier-event idempotency. */
public interface ShipmentRepositoryPort {
    Optional<Shipment> findByShipmentNo(String shipmentNo);

    List<Shipment> findByOrderId(String orderId);

    Shipment save(Shipment shipment);

    /** Atomically claims a provider callback key; false means it was already processed. */
    boolean claimCarrierRequest(String idempotencyKey);

    /** Bounded operational scan for shipments that have not progressed before the SLA cutoff. */
    List<Shipment> findSlaBreaches(LocalDateTime cutoff, int limit);
}
