package com.dnnthanh.marketplace.be.inventory.api.domain.model;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dnnthanh.marketplace.be.platform.exception.BusinessException;
import org.junit.jupiter.api.Test;

/** Inventory invariant tests. */
class InventoryBalanceTest {
    /** Prevents oversell. */
    @Test
    void rejectsOversell() {
        InventoryBalance b = new InventoryBalance(1L, 1L, 1, 0, 0);
        b.reserve(1);
        assertThrows(BusinessException.class, () -> b.reserve(1));
    }
}
