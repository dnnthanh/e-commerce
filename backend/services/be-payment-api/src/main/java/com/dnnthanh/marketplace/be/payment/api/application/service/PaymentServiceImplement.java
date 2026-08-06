package com.dnnthanh.marketplace.be.payment.api.application.service;

import com.dnnthanh.marketplace.be.payment.api.application.command.CreatePaymentCommand;
import com.dnnthanh.marketplace.be.payment.api.application.command.InternalCreatePaymentCommand;
import com.dnnthanh.marketplace.be.payment.api.application.port.in.PaymentUseCase;
import com.dnnthanh.marketplace.be.payment.api.application.port.in.PaymentUseCase.Result;
import com.dnnthanh.marketplace.be.payment.api.application.port.out.PaymentProviderPort;
import com.dnnthanh.marketplace.be.payment.api.application.port.out.PaymentRepositoryPort;
import com.dnnthanh.marketplace.be.payment.api.application.port.out.PaymentWorkflowPersistencePort;
import com.dnnthanh.marketplace.be.payment.api.domain.enumtype.PaymentEventType;
import com.dnnthanh.marketplace.be.payment.api.domain.exception.PaymentNotFoundException;
import com.dnnthanh.marketplace.be.payment.api.domain.model.Payment;
import com.dnnthanh.marketplace.be.platform.context.UserContext;
import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import lombok.RequiredArgsConstructor;

/**
 * Coordinates provider I/O outside local transactions and persists normalized outcomes atomically.
 */
@UseCase
@RequiredArgsConstructor
public class PaymentServiceImplement implements PaymentUseCase {
    private final PaymentRepositoryPort payments;
    private final PaymentWorkflowPersistencePort workflowPersistence;
    private final PaymentProviderRegistry providerRegistry;
    private final UserContext userContext;

    /**
     * Creates a payment for the authenticated caller without leaking identity concerns into the web
     * layer.
     */
    @Override
    public Result createForCurrentUser(CreatePaymentCommand command) {
        return create(
                new InternalCreatePaymentCommand(
                        command.paymentKey(),
                        command.orderId(),
                        userContext.userId(),
                        command.provider(),
                        command.amount()));
    }

    /** Creates an internal payment with an explicitly supplied owner. */
    @Override
    public Result create(InternalCreatePaymentCommand command) {
        Payment existing = payments.findByKey(command.paymentKey()).orElse(null);
        if (existing != null) return new Result(existing, null);

        Payment payment =
                new Payment(
                        command.paymentKey(),
                        command.orderId(),
                        command.userId(),
                        command.provider(),
                        command.amount(),
                        "VND");
        payment.markPending();
        payment = payments.save(payment);

        PaymentProviderPort.ProviderResult result;
        try {
            result = providerRegistry.require(command.provider()).create(payment);
        } catch (RuntimeException ambiguousFailure) {
            payment.markUnknown();
            Payment saved =
                    workflowPersistence.saveWithEvent(payment, PaymentEventType.PAYMENT_UNKNOWN);
            workflowPersistence.scheduleReconciliation(saved, messageOf(ambiguousFailure));
            return new Result(saved, null);
        }

        apply(payment, result);
        Payment saved = workflowPersistence.saveWithEvent(payment, eventType(payment));
        if (saved.status() == Payment.Status.UNKNOWN) {
            workflowPersistence.scheduleReconciliation(saved, "provider returned UNKNOWN");
        }
        return new Result(saved, result.redirectUrl());
    }

    /** Queries provider state for an ambiguous payment rather than blindly charging again. */
    public Result reconcile(String paymentKey) {
        Payment payment =
                payments.findByKey(paymentKey)
                        .orElseThrow(
                                () ->
                                        new PaymentNotFoundException(
                                                "Payment not found: " + paymentKey));
        if (payment.status() != Payment.Status.UNKNOWN
                && payment.status() != Payment.Status.PENDING) {
            return new Result(payment, null);
        }

        PaymentProviderPort.ProviderResult result;
        try {
            result = providerRegistry.require(payment.provider()).query(payment);
        } catch (RuntimeException unavailable) {
            workflowPersistence.scheduleReconciliation(payment, messageOf(unavailable));
            return new Result(payment, null);
        }

        apply(payment, result);
        Payment saved = workflowPersistence.saveWithEvent(payment, eventType(payment));
        if (saved.status() == Payment.Status.UNKNOWN || saved.status() == Payment.Status.PENDING) {
            workflowPersistence.scheduleReconciliation(saved, "provider state remains ambiguous");
        } else {
            workflowPersistence.clearReconciliation(saved);
        }
        return new Result(saved, null);
    }

    private void apply(Payment payment, PaymentProviderPort.ProviderResult result) {
        switch (result.status()) {
            case SUCCESS -> payment.markPaid(result.transactionId());
            case FAILED -> payment.markFailed();
            case UNKNOWN -> payment.markUnknown();
            case PENDING -> payment.markPending();
        }
    }

    private PaymentEventType eventType(Payment payment) {
        return switch (payment.status()) {
            case PAID, PARTIALLY_REFUNDED, REFUNDED -> PaymentEventType.PAYMENT_SUCCEEDED;
            case FAILED -> PaymentEventType.PAYMENT_FAILED;
            case UNKNOWN -> PaymentEventType.PAYMENT_UNKNOWN;
            case CREATED, PENDING, AUTHORIZED -> PaymentEventType.PAYMENT_PENDING;
        };
    }

    private static String messageOf(RuntimeException failure) {
        return failure.getMessage() == null
                ? failure.getClass().getSimpleName()
                : failure.getMessage();
    }
}
