package com.dnnthanh.marketplace.be.seller.api.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dnnthanh.marketplace.be.seller.api.domain.enumtype.SellerStatus;
import com.dnnthanh.marketplace.be.seller.api.domain.exception.SellerStateConflictException;
import org.junit.jupiter.api.Test;

class SellerAccountTest {

    @Test
    void managesSellerLifecycleWithoutOwningAuthorization() {
        SellerAccount seller = new SellerAccount(10001L);

        seller.submit();
        seller.verify();
        seller.activate();
        seller.suspend();
        seller.reinstate();

        assertEquals(SellerStatus.ACTIVE, seller.status());
    }

    @Test
    void rejectsActivationBeforeVerification() {
        SellerAccount seller = new SellerAccount(10001L);
        assertThrows(SellerStateConflictException.class, seller::activate);
    }
}
