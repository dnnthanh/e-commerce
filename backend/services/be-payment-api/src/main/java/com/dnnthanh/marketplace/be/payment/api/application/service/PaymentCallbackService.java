package com.dnnthanh.marketplace.be.payment.api.application.service;

import com.dnnthanh.marketplace.be.payment.api.application.port.out.PaymentRepositoryPort;
import com.dnnthanh.marketplace.be.payment.api.application.port.out.PaymentWorkflowPersistencePort;
import com.dnnthanh.marketplace.be.payment.api.domain.enumtype.PaymentEventType;
import com.dnnthanh.marketplace.be.payment.api.domain.exception.PaymentNotFoundException;
import com.dnnthanh.marketplace.be.payment.api.domain.model.Payment;
import com.dnnthanh.marketplace.be.payment.api.domain.model.Payment.ProviderOutcome;
import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

/** Idempotent provider callback handler with inbox + aggregate + outbox in one DB transaction. */
@UseCase
@RequiredArgsConstructor
public class PaymentCallbackService {
    private final PaymentRepositoryPort payments;
    private final PaymentWorkflowPersistencePort workflow;

    @Transactional
    public boolean handle(String eventId, String paymentKey, ProviderOutcome outcome) {
        return handle(eventId, paymentKey, outcome, null);
    }

    @Transactional
    public boolean handle(
            String eventId,
            String paymentKey,
            ProviderOutcome outcome,
            String providerTransactionId) {
        if (!workflow.claimProviderEvent(eventId)) {
            return false;
        }
        Payment payment =
                payments.findByKey(paymentKey)
                        .orElseThrow(
                                () ->
                                        new PaymentNotFoundException(
                                                "Payment not found: " + paymentKey));
        payment.applyProviderEvent(outcome, providerTransactionId);
        workflow.saveWithEvent(payment, eventType(payment));
        if (payment.status() == Payment.Status.UNKNOWN) {
            workflow.scheduleReconciliation(payment, "provider callback returned UNKNOWN");
        } else {
            workflow.clearReconciliation(payment);
        }
        return true;
    }

    private PaymentEventType eventType(Payment payment) {
        return switch (payment.status()) {
            case PAID -> PaymentEventType.PAYMENT_SUCCEEDED;
            case FAILED -> PaymentEventType.PAYMENT_FAILED;
            case UNKNOWN -> PaymentEventType.PAYMENT_UNKNOWN;
            case PARTIALLY_REFUNDED, REFUNDED -> PaymentEventType.PAYMENT_REFUNDED;
            case CREATED, PENDING, AUTHORIZED -> PaymentEventType.PAYMENT_PENDING;
        };
    }
}
