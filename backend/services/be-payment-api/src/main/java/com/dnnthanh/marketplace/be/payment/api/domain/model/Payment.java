package com.dnnthanh.marketplace.be.payment.api.domain.model;

import com.dnnthanh.marketplace.be.payment.api.domain.exception.InvalidPaymentException;
import com.dnnthanh.marketplace.be.payment.api.domain.exception.InvalidRefundAmountException;
import com.dnnthanh.marketplace.be.payment.api.domain.exception.PaymentStateConflictException;
import com.dnnthanh.marketplace.be.platform.model.CodeEnum;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;

/** Canonical payment aggregate for provider outcomes, reconciliation and cumulative refunds. */
public final class Payment {
    private Long id;
    private final String paymentKey;
    private final String orderId;
    private final String userId;
    private final Provider provider;
    private final BigDecimal amount;
    private final String currency;
    private Status status;
    private String providerTransactionId;
    private BigDecimal refundedAmount;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum Provider implements CodeEnum {
        MOMO,
        VNPAY,
        VIETQR
    }

    public enum Status implements CodeEnum {
        CREATED,
        PENDING,
        AUTHORIZED,
        PAID,
        UNKNOWN,
        FAILED,
        PARTIALLY_REFUNDED,
        REFUNDED
    }

    /** Provider-neutral callback/reconciliation outcome. CAPTURED maps to persisted PAID. */
    public enum ProviderOutcome implements CodeEnum {
        PENDING,
        AUTHORIZED,
        CAPTURED,
        FAILED,
        UNKNOWN
    }

    public Payment(
            String paymentKey,
            String orderId,
            String userId,
            Provider provider,
            BigDecimal amount,
            String currency) {
        this(
                null,
                paymentKey,
                orderId,
                userId,
                provider,
                amount,
                currency,
                Status.CREATED,
                null,
                BigDecimal.ZERO,
                LocalDateTime.now(),
                LocalDateTime.now());
    }

    private Payment(
            Long id,
            String paymentKey,
            String orderId,
            String userId,
            Provider provider,
            BigDecimal amount,
            String currency,
            Status status,
            String providerTransactionId,
            BigDecimal refundedAmount,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        if (paymentKey == null
                || paymentKey.isBlank()
                || orderId == null
                || orderId.isBlank()
                || userId == null
                || userId.isBlank()) {
            throw new InvalidPaymentException("paymentKey, orderId and userId are required");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new InvalidPaymentException("amount must be positive");
        }
        this.id = id;
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.userId = userId;
        this.provider = Objects.requireNonNull(provider);
        this.amount = money(amount);
        this.currency = Objects.requireNonNull(currency);
        this.status = Objects.requireNonNull(status);
        this.providerTransactionId = providerTransactionId;
        this.refundedAmount = money(Objects.requireNonNull(refundedAmount));
        if (this.refundedAmount.signum() < 0 || this.refundedAmount.compareTo(this.amount) > 0) {
            throw new InvalidRefundAmountException(
                    "Persisted refund total is outside payment amount");
        }
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public void markPending() {
        applyProviderEvent(ProviderOutcome.PENDING, null);
    }

    public void markPaid(String transactionId) {
        applyProviderEvent(ProviderOutcome.CAPTURED, transactionId);
    }

    public void markUnknown() {
        applyProviderEvent(ProviderOutcome.UNKNOWN, null);
    }

    public void markFailed() {
        applyProviderEvent(ProviderOutcome.FAILED, null);
    }

    /** Applies a provider callback/reconciliation result through one legal state machine. */
    public void applyProviderEvent(ProviderOutcome outcome, String transactionId) {
        Objects.requireNonNull(outcome);
        switch (outcome) {
            case PENDING ->
                    requireTransitionTo(
                            Status.PENDING, Status.CREATED, Status.PENDING, Status.UNKNOWN);
            case AUTHORIZED ->
                    requireTransitionTo(
                            Status.AUTHORIZED,
                            Status.CREATED,
                            Status.PENDING,
                            Status.UNKNOWN,
                            Status.AUTHORIZED);
            case CAPTURED -> {
                requireTransitionTo(
                        Status.PAID,
                        Status.CREATED,
                        Status.PENDING,
                        Status.AUTHORIZED,
                        Status.UNKNOWN,
                        Status.PAID);
                if (transactionId != null && !transactionId.isBlank()) {
                    providerTransactionId = transactionId;
                }
            }
            case FAILED ->
                    requireTransitionTo(
                            Status.FAILED,
                            Status.CREATED,
                            Status.PENDING,
                            Status.UNKNOWN,
                            Status.FAILED);
            case UNKNOWN -> {
                if (status == Status.PARTIALLY_REFUNDED || status == Status.REFUNDED) {
                    throw new PaymentStateConflictException(
                            "Refunded payment cannot become UNKNOWN");
                }
                if (status == Status.PAID) {
                    throw new PaymentStateConflictException(
                            "Captured payment cannot become UNKNOWN");
                }
                status = Status.UNKNOWN;
            }
        }
        updatedAt = LocalDateTime.now();
    }

    /** Applies a confirmed refund and enforces the cumulative refund invariant in the aggregate. */
    public void applySuccessfulRefund(BigDecimal value) {
        if (status != Status.PAID && status != Status.PARTIALLY_REFUNDED) {
            throw new PaymentStateConflictException("Payment is not refundable from " + status);
        }
        BigDecimal normalized = money(value);
        if (normalized.signum() <= 0 || refundedAmount.add(normalized).compareTo(amount) > 0) {
            throw new InvalidRefundAmountException("Refund exceeds captured amount");
        }
        refundedAmount = refundedAmount.add(normalized);
        status =
                refundedAmount.compareTo(amount) == 0 ? Status.REFUNDED : Status.PARTIALLY_REFUNDED;
        updatedAt = LocalDateTime.now();
    }

    /** Backward-compatible state helpers delegate to the cumulative model. */
    public void markPartiallyRefunded() {
        if (refundedAmount.signum() == 0 || refundedAmount.compareTo(amount) >= 0) {
            throw new PaymentStateConflictException("Partial refund total is not valid");
        }
        status = Status.PARTIALLY_REFUNDED;
        updatedAt = LocalDateTime.now();
    }

    public void markRefunded() {
        if (refundedAmount.compareTo(amount) != 0) {
            throw new PaymentStateConflictException(
                    "Full refund requires cumulative refund = payment amount");
        }
        status = Status.REFUNDED;
        updatedAt = LocalDateTime.now();
    }

    private void requireTransitionTo(Status target, Status... allowed) {
        for (Status current : allowed) {
            if (status == current) {
                status = target;
                return;
            }
        }
        throw new PaymentStateConflictException(
                "Provider outcome cannot transition payment from " + status + " to " + target);
    }

    private static BigDecimal money(BigDecimal value) {
        return Objects.requireNonNull(value).setScale(2, RoundingMode.HALF_UP);
    }

    public static Payment rehydrate(
            Long id,
            String key,
            String order,
            String user,
            Provider provider,
            BigDecimal amount,
            String currency,
            Status status,
            String tx,
            BigDecimal refundedAmount,
            LocalDateTime created,
            LocalDateTime updated) {
        return new Payment(
                id,
                key,
                order,
                user,
                provider,
                amount,
                currency,
                status,
                tx,
                refundedAmount,
                created,
                updated);
    }

    public Long id() {
        return id;
    }

    public String paymentKey() {
        return paymentKey;
    }

    public String orderId() {
        return orderId;
    }

    public String userId() {
        return userId;
    }

    public Provider provider() {
        return provider;
    }

    public BigDecimal amount() {
        return amount;
    }

    public String currency() {
        return currency;
    }

    public Status status() {
        return status;
    }

    public String providerTransactionId() {
        return providerTransactionId;
    }

    public BigDecimal refundedAmount() {
        return refundedAmount;
    }

    public LocalDateTime createdAt() {
        return createdAt;
    }

    public LocalDateTime updatedAt() {
        return updatedAt;
    }
}
