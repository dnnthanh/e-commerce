package com.dnnthanh.marketplace.be.inventory.api.domain.enumtype;

import com.dnnthanh.marketplace.be.platform.model.CodeEnum;

/** Inventory reservation lifecycle. */
public enum ReservationStatus implements CodeEnum {
    RESERVED,
    CONFIRMED,
    RELEASED,
    EXPIRED
}
