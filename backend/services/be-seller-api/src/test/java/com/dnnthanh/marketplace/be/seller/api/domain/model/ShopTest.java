package com.dnnthanh.marketplace.be.seller.api.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dnnthanh.marketplace.be.seller.api.domain.enumtype.SellerStatus;
import com.dnnthanh.marketplace.be.seller.api.domain.exception.InvalidShopException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/** Shop tests. */
class ShopTest {
    @Test
    void detectsMaterialChange() {
        Shop shop =
                new Shop(1L, 10L, "demo", "Old", "Desc", SellerStatus.ACTIVE, LocalDateTime.now());

        assertTrue(shop.updateMaterialInfo("New", "Changed"));
        assertEquals("New", shop.getName());
    }

    @Test
    void rejectsBlankName() {
        Shop shop =
                new Shop(1L, 10L, "demo", "Old", null, SellerStatus.ACTIVE, LocalDateTime.now());

        assertThrows(InvalidShopException.class, () -> shop.updateMaterialInfo("   ", null));
    }
}
