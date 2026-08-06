package com.dnnthanh.marketplace.be.checkout.api.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.dnnthanh.marketplace.be.checkout.api.domain.enumtype.CheckoutPaymentStatus;
import org.junit.jupiter.api.Test;

/** Saga state tests. */
class CheckoutSagaTest {
    @Test
    void unknownPaymentRemainsRecoverable() {
        CheckoutSaga saga = new CheckoutSaga("C1", "U1");
        saga.reserved();
        saga.ordered("O1");
        saga.payment("P1", CheckoutPaymentStatus.UNKNOWN);
        assertEquals(CheckoutSaga.State.PAYMENT_UNKNOWN, saga.state());
        assertEquals("O1", saga.orderNo());
    }

    @Test
    void retryUsesDurableBackoffState() {
        CheckoutSaga saga = new CheckoutSaga("C1", "U1");
        saga.retryLater();
        assertEquals(1, saga.retryCount());
        assertNotNull(saga.nextRetryAt());
    }
}
