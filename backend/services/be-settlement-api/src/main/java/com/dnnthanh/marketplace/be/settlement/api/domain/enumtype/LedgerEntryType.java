package com.dnnthanh.marketplace.be.settlement.api.domain.enumtype;

import com.dnnthanh.marketplace.be.platform.model.CodeEnum;

/** Immutable seller payable ledger entry kinds. */
public enum LedgerEntryType implements CodeEnum {
    SALE,
    COMMISSION,
    SHIPPING_CHARGE,
    DISCOUNT_SHARE,
    REFUND,
    MANUAL_ADJUSTMENT,
    HOLD,
    RELEASE
}
