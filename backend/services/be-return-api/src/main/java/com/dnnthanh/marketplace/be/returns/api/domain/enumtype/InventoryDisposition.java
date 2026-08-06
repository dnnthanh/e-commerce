package com.dnnthanh.marketplace.be.returns.api.domain.enumtype;

import com.dnnthanh.marketplace.be.platform.model.CodeEnum;

/** Inventory handling decision produced by physical return inspection. */
public enum InventoryDisposition implements CodeEnum {
    NONE,
    RESTOCK,
    QUARANTINE,
    SCRAP
}
