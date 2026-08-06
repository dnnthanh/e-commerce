package com.dnnthanh.marketplace.be.inventory.api.domain.enumtype;

import com.dnnthanh.marketplace.be.platform.model.CodeEnum;

/** Auditable reasons for physical stock adjustments. */
public enum InventoryAdjustmentReason implements CodeEnum {
    CYCLE_COUNT,
    DAMAGE,
    FOUND,
    CORRECTION,
    RETURN_RESTOCK
}
