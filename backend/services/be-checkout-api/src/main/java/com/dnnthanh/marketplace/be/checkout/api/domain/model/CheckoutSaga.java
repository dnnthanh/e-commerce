package com.dnnthanh.marketplace.be.checkout.api.domain.model;

import com.dnnthanh.marketplace.be.checkout.api.domain.enumtype.CheckoutPaymentStatus;
import com.dnnthanh.marketplace.be.platform.model.CodeEnum;
import java.time.LocalDateTime;
import java.util.Objects;

/** Persistent checkout process state; remote effects are resumable from durable checkpoints. */
public final class CheckoutSaga {
    private final String checkoutKey;
    private final String userId;
    private State state;
    private String orderNo;
    private String paymentKey;
    private int retryCount;
    private LocalDateTime nextRetryAt;

    public CheckoutSaga(String key, String userId) {
        this.checkoutKey = Objects.requireNonNull(key);
        this.userId = Objects.requireNonNull(userId);
        this.state = State.STARTED;
    }

    public static CheckoutSaga rehydrate(
            String key,
            String user,
            State state,
            String order,
            String payment,
            int retry,
            LocalDateTime next) {
        CheckoutSaga saga = new CheckoutSaga(key, user);
        saga.state = state;
        saga.orderNo = order;
        saga.paymentKey = payment;
        saga.retryCount = retry;
        saga.nextRetryAt = next;
        return saga;
    }

    public void reserved() {
        state = State.RESERVED;
    }

    public void ordered(String orderNo) {
        this.orderNo = orderNo;
        state = State.ORDER_CREATED;
    }

    public void payment(String key, CheckoutPaymentStatus status) {
        paymentKey = key;
        state =
                switch (status) {
                    case UNKNOWN -> State.PAYMENT_UNKNOWN;
                    case PAID -> State.COMPLETED;
                    case PENDING -> State.PAYMENT_PENDING;
                    case FAILED -> State.FAILED_RETRYABLE;
                };
    }

    public void retryLater() {
        state = State.FAILED_RETRYABLE;
        retryCount++;
        nextRetryAt = LocalDateTime.now().plusSeconds(Math.min(300, 30L * retryCount));
    }

    public void compensated() {
        state = State.COMPENSATED;
    }

    public enum State implements CodeEnum {
        STARTED,
        RESERVED,
        ORDER_CREATED,
        PAYMENT_PENDING,
        PAYMENT_UNKNOWN,
        COMPLETED,
        FAILED_RETRYABLE,
        COMPENSATED
    }

    public String checkoutKey() {
        return checkoutKey;
    }

    public String userId() {
        return userId;
    }

    public State state() {
        return state;
    }

    public String orderNo() {
        return orderNo;
    }

    public String paymentKey() {
        return paymentKey;
    }

    public int retryCount() {
        return retryCount;
    }

    public LocalDateTime nextRetryAt() {
        return nextRetryAt;
    }
}
