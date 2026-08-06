package com.dnnthanh.marketplace.be.returns.api.application.port.out;

import com.dnnthanh.marketplace.be.returns.api.domain.enumtype.ReturnStatus;
import java.math.BigDecimal;

/** Row-lock based persistence operations reserved for ambiguous payment refund outcomes. */
public interface ReturnWorkflowPersistencePort {
    RefundCandidate prepareRefund(String returnKey);

    void markRefundOutcome(String returnKey, ReturnStatus status);

    record RefundCandidate(String orderId, BigDecimal refundableAmount) {}
}
