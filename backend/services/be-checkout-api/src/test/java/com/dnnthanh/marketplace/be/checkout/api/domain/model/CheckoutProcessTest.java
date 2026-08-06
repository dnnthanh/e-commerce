package com.dnnthanh.marketplace.be.checkout.api.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dnnthanh.marketplace.be.checkout.api.domain.enumtype.CheckoutStep;
import org.junit.jupiter.api.Test;

class CheckoutProcessTest {
    @Test
    void derivesCompensationAfterPaymentInitiation() {
        CheckoutProcess process = new CheckoutProcess("C", "KEY");
        process.quoted();
        process.promotionReserved();
        process.inventoryReserved();
        process.paymentInitiated();
        var plan = process.fail("timeout");
        assertTrue(plan.releaseInventory());
        assertTrue(plan.releasePromotion());
        assertTrue(plan.reconcilePayment());
        process.compensationCompleted();
        assertEquals(CheckoutStep.FAILED, process.step());
    }
}
