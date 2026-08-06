package com.dnnthanh.marketplace.be.cart.api.domain.model;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dnnthanh.marketplace.be.cart.api.domain.exception.CartVersionConflictException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ShoppingCartTest {
    @Test
    void rejectsLostUpdateFromStaleTab() {
        ShoppingCart cart = new ShoppingCart("C", "U");
        cart.addOrReplace(new ShoppingCart.CartLine(1L, "SKU", 1, BigDecimal.TEN, true), 0);
        assertThrows(CartVersionConflictException.class, () -> cart.remove(1L, "SKU", 0));
    }
}
