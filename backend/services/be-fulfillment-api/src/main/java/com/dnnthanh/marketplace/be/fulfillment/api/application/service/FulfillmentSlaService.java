package com.dnnthanh.marketplace.be.fulfillment.api.application.service;

import com.dnnthanh.marketplace.be.fulfillment.api.application.port.out.ShipmentRepositoryPort;
import com.dnnthanh.marketplace.be.fulfillment.api.domain.exception.InvalidShipmentException;
import com.dnnthanh.marketplace.be.fulfillment.api.domain.model.Shipment;
import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;

/** Operational query for shipments whose lifecycle has stopped progressing beyond the SLA. */
@UseCase
@RequiredArgsConstructor
public class FulfillmentSlaService {
    private static final int MAX_BREACHES = 200;
    private final ShipmentRepositoryPort shipmentRepository;
    private final Clock clock;

    public List<Shipment> findBreaches(int staleMinutes) {
        if (staleMinutes <= 0) {
            throw new InvalidShipmentException("staleMinutes must be positive");
        }
        return shipmentRepository.findSlaBreaches(
                LocalDateTime.now(clock).minusMinutes(staleMinutes), MAX_BREACHES);
    }
}
