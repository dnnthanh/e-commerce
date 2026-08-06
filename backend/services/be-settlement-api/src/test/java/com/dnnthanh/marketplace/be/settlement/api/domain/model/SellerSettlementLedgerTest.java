package com.dnnthanh.marketplace.be.settlement.api.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dnnthanh.marketplace.be.settlement.api.domain.enumtype.LedgerEntryType;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class SellerSettlementLedgerTest {
    @Test
    void rebuildsBalanceFromImmutableIdempotentLedger() {
        SellerSettlementLedger l = new SellerSettlementLedger(1L);
        assertTrue(l.append("E1", LedgerEntryType.SALE, new BigDecimal("100"), "O"));
        assertFalse(l.append("E1", LedgerEntryType.SALE, new BigDecimal("100"), "O"));
        l.append("E2", LedgerEntryType.COMMISSION, new BigDecimal("-10"), "O");
        assertEquals(new BigDecimal("90.00"), l.payableBalance());
    }
}
