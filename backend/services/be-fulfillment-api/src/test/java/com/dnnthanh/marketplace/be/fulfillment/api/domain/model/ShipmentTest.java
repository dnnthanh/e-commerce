package com.dnnthanh.marketplace.be.fulfillment.api.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dnnthanh.marketplace.be.fulfillment.api.domain.enumtype.ShipmentStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class ShipmentTest {
    @Test
    void ignoresOutOfOrderCarrierWebhook() {
        Shipment shipment =
                new Shipment("S1", "O1", 1L, 2L, List.of(new Shipment.ShipmentLine(10L, "SKU", 1)));
        shipment.transitionTo(ShipmentStatus.PICKING, LocalDateTime.now());
        shipment.transitionTo(ShipmentStatus.PACKED, LocalDateTime.now());
        shipment.transitionTo(ShipmentStatus.READY_TO_SHIP, LocalDateTime.now());
        shipment.transitionTo(ShipmentStatus.HANDED_OVER, LocalDateTime.now());
        assertTrue(shipment.applyCarrierEvent(5, ShipmentStatus.IN_TRANSIT, LocalDateTime.now()));
        assertFalse(shipment.applyCarrierEvent(4, ShipmentStatus.DELIVERED, LocalDateTime.now()));
        assertEquals(ShipmentStatus.IN_TRANSIT, shipment.status());
    }
}
