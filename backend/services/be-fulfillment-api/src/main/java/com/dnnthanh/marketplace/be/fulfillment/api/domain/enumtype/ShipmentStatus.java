package com.dnnthanh.marketplace.be.fulfillment.api.domain.enumtype;

import com.dnnthanh.marketplace.be.platform.model.CodeEnum;

/** Lifecycle for one shipment/package. */
public enum ShipmentStatus implements CodeEnum {
    ALLOCATED,
    PICKING,
    PACKED,
    READY_TO_SHIP,
    HANDED_OVER,
    IN_TRANSIT,
    DELIVERED,
    DELIVERY_FAILED,
    RETURN_TO_SENDER,
    CANCELLED;

    /** Returns whether this shipment may move to {@code next}. */
    public boolean canTransitionTo(ShipmentStatus next) {
        return switch (this) {
            case ALLOCATED -> next == PICKING || next == CANCELLED;
            case PICKING -> next == PACKED || next == CANCELLED;
            case PACKED -> next == READY_TO_SHIP || next == CANCELLED;
            case READY_TO_SHIP -> next == HANDED_OVER || next == CANCELLED;
            case HANDED_OVER -> next == IN_TRANSIT;
            case IN_TRANSIT -> next == DELIVERED || next == DELIVERY_FAILED;
            case DELIVERY_FAILED -> next == IN_TRANSIT || next == RETURN_TO_SENDER;
            case DELIVERED, RETURN_TO_SENDER, CANCELLED -> false;
        };
    }
}
