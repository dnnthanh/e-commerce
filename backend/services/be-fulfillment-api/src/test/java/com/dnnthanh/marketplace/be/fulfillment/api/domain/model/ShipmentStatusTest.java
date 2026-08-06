package com.dnnthanh.marketplace.be.fulfillment.api.domain.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dnnthanh.marketplace.be.fulfillment.api.domain.enumtype.ShipmentStatus;
import org.junit.jupiter.api.Test;

/** Shipment lifecycle tests. */
class ShipmentStatusTest {
    @Test
    void deliveredIsTerminal() {
        assertFalse(ShipmentStatus.DELIVERED.canTransitionTo(ShipmentStatus.IN_TRANSIT));
    }

    @Test
    void allocatedMustBePickedBeforePacking() {
        assertTrue(ShipmentStatus.ALLOCATED.canTransitionTo(ShipmentStatus.PICKING));
        assertFalse(ShipmentStatus.ALLOCATED.canTransitionTo(ShipmentStatus.PACKED));
    }
}
