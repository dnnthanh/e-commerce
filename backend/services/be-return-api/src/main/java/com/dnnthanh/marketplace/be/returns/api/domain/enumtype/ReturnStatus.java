package com.dnnthanh.marketplace.be.returns.api.domain.enumtype;

import com.dnnthanh.marketplace.be.platform.model.CodeEnum;

/** Return/refund orchestration lifecycle. */
public enum ReturnStatus implements CodeEnum {
    REQUESTED,
    APPROVED,
    REJECTED,
    IN_TRANSIT,
    RECEIVED,
    INSPECTED,
    DISPUTED,
    REFUND_PENDING,
    REFUND_UNKNOWN,
    REFUND_FAILED,
    REFUNDED,
    COMPLETED,
    CLOSED;

    /** Whether an idempotent refund attempt may be started or retried from this state. */
    public boolean canPrepareRefund() {
        return this == INSPECTED
                || this == REFUND_PENDING
                || this == REFUND_UNKNOWN
                || this == REFUND_FAILED;
    }
}
