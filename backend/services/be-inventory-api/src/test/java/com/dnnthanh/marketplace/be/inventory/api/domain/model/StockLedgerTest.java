package com.dnnthanh.marketplace.be.inventory.api.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dnnthanh.marketplace.be.inventory.api.domain.exception.InsufficientStockException;
import org.junit.jupiter.api.Test;

class StockLedgerTest {
    @Test
    void preventsOversellAndMakesReservationIdempotent() {
        StockLedger ledger = new StockLedger("SKU", 1L, 2);
        assertTrue(ledger.reserve("R1", 2));
        assertFalse(ledger.reserve("R1", 2));
        assertThrows(InsufficientStockException.class, () -> ledger.reserve("R2", 1));
        assertEquals(0, ledger.available());
    }
}
