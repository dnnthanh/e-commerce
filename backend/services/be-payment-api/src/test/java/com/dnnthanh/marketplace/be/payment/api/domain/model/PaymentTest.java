package com.dnnthanh.marketplace.be.payment.api.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dnnthanh.marketplace.be.payment.api.domain.exception.InvalidRefundAmountException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PaymentTest {
    @Test
    void providerCaptureAndCumulativeRefundShareOneAggregate() {
        Payment payment =
                new Payment(
                        "P-1", "O-1", "U-1", Payment.Provider.MOMO, new BigDecimal("100"), "VND");
        payment.applyProviderEvent(Payment.ProviderOutcome.AUTHORIZED, "TX-1");
        payment.applyProviderEvent(Payment.ProviderOutcome.CAPTURED, "TX-1");
        payment.applySuccessfulRefund(new BigDecimal("30"));
        assertEquals(Payment.Status.PARTIALLY_REFUNDED, payment.status());
        assertEquals(new BigDecimal("30.00"), payment.refundedAmount());
        payment.applySuccessfulRefund(new BigDecimal("70"));
        assertEquals(Payment.Status.REFUNDED, payment.status());
        assertEquals(new BigDecimal("100.00"), payment.refundedAmount());
    }

    @Test
    void rejectsRefundAboveCapturedAmount() {
        Payment payment =
                new Payment(
                        "P-2", "O-2", "U-2", Payment.Provider.VNPAY, new BigDecimal("100"), "VND");
        payment.applyProviderEvent(Payment.ProviderOutcome.CAPTURED, "TX-2");
        assertThrows(
                InvalidRefundAmountException.class,
                () -> payment.applySuccessfulRefund(new BigDecimal("101")));
    }
}
