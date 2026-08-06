package com.dnnthanh.marketplace.be.payment.api.application.port.out;

import com.dnnthanh.marketplace.be.payment.api.domain.enumtype.RefundStatus;
import com.dnnthanh.marketplace.be.payment.api.domain.model.Payment;
import java.math.BigDecimal;
import java.util.Optional;

/** Transactional refund ledger/outbox persistence boundary. */
public interface RefundPersistencePort {
    Optional<RefundSnapshot> findRefundByKey(String refundKey);

    RefundSnapshot persistOutcome(
            Payment payment,
            String refundKey,
            String returnKey,
            BigDecimal amount,
            RefundStatus status,
            String providerRefundId);

    record RefundSnapshot(
            String refundKey,
            RefundStatus status,
            BigDecimal amount,
            Payment.Status paymentStatus) {}
}
