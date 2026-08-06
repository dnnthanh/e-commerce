package com.dnnthanh.marketplace.be.payment.api.application.service;

import com.dnnthanh.marketplace.be.payment.api.application.port.in.RefundUseCase;
import com.dnnthanh.marketplace.be.payment.api.application.port.in.RefundUseCase.RefundResult;
import com.dnnthanh.marketplace.be.payment.api.application.port.out.PaymentProviderPort;
import com.dnnthanh.marketplace.be.payment.api.application.port.out.PaymentRepositoryPort;
import com.dnnthanh.marketplace.be.payment.api.application.port.out.RefundPersistencePort;
import com.dnnthanh.marketplace.be.payment.api.domain.enumtype.RefundStatus;
import com.dnnthanh.marketplace.be.payment.api.domain.exception.InvalidRefundAmountException;
import com.dnnthanh.marketplace.be.payment.api.domain.exception.PaymentNotFoundException;
import com.dnnthanh.marketplace.be.payment.api.domain.exception.PaymentNotRefundableException;
import com.dnnthanh.marketplace.be.payment.api.domain.model.Payment;
import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;

/**
 * Coordinates idempotent partial/full refunds without holding a DB transaction during provider I/O.
 */
@UseCase
@RequiredArgsConstructor
public class RefundServiceImplement implements RefundUseCase {
    private final PaymentRepositoryPort payments;
    private final RefundPersistencePort refunds;
    private final PaymentProviderRegistry providerRegistry;

    public RefundResult refund(
            String refundKey, String orderId, String returnKey, BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new InvalidRefundAmountException("Refund amount must be positive");
        }
        RefundPersistencePort.RefundSnapshot existing =
                refunds.findRefundByKey(refundKey).orElse(null);
        if (existing != null) return toResult(existing);

        Payment payment =
                payments.findByOrder(orderId)
                        .orElseThrow(
                                () ->
                                        new PaymentNotFoundException(
                                                "Payment not found for order " + orderId));
        if (payment.status() != Payment.Status.PAID
                && payment.status() != Payment.Status.PARTIALLY_REFUNDED) {
            throw new PaymentNotRefundableException(
                    "Payment is not refundable: " + payment.status());
        }

        PaymentProviderPort.ProviderResult providerResult;
        try {
            providerResult =
                    providerRegistry.require(payment.provider()).refund(payment, refundKey, amount);
        } catch (RuntimeException ambiguous) {
            return toResult(
                    refunds.persistOutcome(
                            payment, refundKey, returnKey, amount, RefundStatus.UNKNOWN, null));
        }

        RefundStatus status =
                switch (providerResult.status()) {
                    case SUCCESS -> RefundStatus.SUCCEEDED;
                    case FAILED -> RefundStatus.FAILED;
                    case UNKNOWN, PENDING -> RefundStatus.UNKNOWN;
                };
        return toResult(
                refunds.persistOutcome(
                        payment,
                        refundKey,
                        returnKey,
                        amount,
                        status,
                        providerResult.transactionId()));
    }

    private RefundResult toResult(RefundPersistencePort.RefundSnapshot value) {
        return new RefundResult(
                value.refundKey(), value.status(), value.amount(), value.paymentStatus());
    }
}
