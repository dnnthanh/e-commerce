package com.dnnthanh.marketplace.be.settlement.api.domain.enumtype;

import com.dnnthanh.marketplace.be.platform.model.CodeEnum;

/** Settlement period lifecycle. */
public enum SettlementStatus implements CodeEnum {
    OPEN,
    APPROVED,
    CLOSED,
    ON_HOLD
}
