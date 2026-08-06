package com.dnnthanh.marketplace.be.payment.api.application.port.out;

import com.dnnthanh.marketplace.be.payment.api.domain.enumtype.PaymentEventType;
import com.dnnthanh.marketplace.be.payment.api.domain.model.Payment;

/** Atomic payment/outbox/inbox/reconciliation persistence operations. */
public interface PaymentWorkflowPersistencePort {
    Payment saveWithEvent(Payment payment, PaymentEventType eventType);

    /**
     * Claims a provider event in the current transaction. Rollback also rolls the inbox claim back.
     */
    boolean claimProviderEvent(String eventId);

    void scheduleReconciliation(Payment payment, String error);

    void clearReconciliation(Payment payment);
}
