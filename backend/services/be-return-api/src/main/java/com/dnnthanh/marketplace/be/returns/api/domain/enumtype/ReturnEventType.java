package com.dnnthanh.marketplace.be.returns.api.domain.enumtype;

import com.dnnthanh.marketplace.be.platform.model.CodeEnum;

/** Durable integration events emitted by the return bounded context. */
public enum ReturnEventType implements CodeEnum {
    RETURN_REQUESTED,
    RETURN_APPROVED,
    RETURN_REJECTED,
    RETURN_RECEIVED,
    RETURN_INSPECTED,
    RETURN_DISPUTED,
    RETURN_DISPUTE_RESOLVED,
    RETURN_REFUND_REQUESTED,
    RETURN_REFUND_UNKNOWN,
    RETURN_REFUND_FAILED,
    RETURN_COMPLETED,
    RETURN_ITEM_RESTOCK_REQUESTED,
    RETURN_ITEM_QUARANTINE_REQUESTED,
    RETURN_ITEM_SCRAP_REQUESTED
}
