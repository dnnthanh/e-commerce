package com.dnnthanh.marketplace.be.fulfillment.api.domain.enumtype;

import com.dnnthanh.marketplace.be.fulfillment.api.domain.exception.InvalidShipmentException;
import com.dnnthanh.marketplace.be.platform.model.CodeEnum;

/** Stable fulfillment integration events. */
public enum FulfillmentEventType implements CodeEnum {
    SHIPMENT_PICKING,
    SHIPMENT_PACKED,
    SHIPMENT_READY_TO_SHIP,
    SHIPMENT_HANDED_OVER,
    SHIPMENT_IN_TRANSIT,
    SHIPMENT_DELIVERED,
    SHIPMENT_DELIVERY_FAILED,
    SHIPMENT_RETURN_TO_SENDER,
    SHIPMENT_CANCELLED,
    SELLER_FULFILLMENT_COMPLETED;

    public static FulfillmentEventType forShipmentStatus(ShipmentStatus status) {
        return switch (status) {
            case PICKING -> SHIPMENT_PICKING;
            case PACKED -> SHIPMENT_PACKED;
            case READY_TO_SHIP -> SHIPMENT_READY_TO_SHIP;
            case HANDED_OVER -> SHIPMENT_HANDED_OVER;
            case IN_TRANSIT -> SHIPMENT_IN_TRANSIT;
            case DELIVERED -> SHIPMENT_DELIVERED;
            case DELIVERY_FAILED -> SHIPMENT_DELIVERY_FAILED;
            case RETURN_TO_SENDER -> SHIPMENT_RETURN_TO_SENDER;
            case CANCELLED -> SHIPMENT_CANCELLED;
            case ALLOCATED ->
                    throw new InvalidShipmentException(
                            "ALLOCATED is creation state, not transition event");
        };
    }
}
