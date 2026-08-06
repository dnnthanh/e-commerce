package com.dnnthanh.marketplace.be.catalog.api.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dnnthanh.marketplace.be.catalog.api.domain.exception.ProductStateConflictException;
import org.junit.jupiter.api.Test;

class ProductTest {

    @Test
    void rejectsPublishWhenMediaIsNotReady() {
        Product product = new Product(1L, 2L, "Phone", "Demo");

        ProductStateConflictException exception =
                assertThrows(ProductStateConflictException.class, () -> product.publish(false));

        assertEquals("Required product media is not ready", exception.getMessage());
    }

    @Test
    void publishesWhenMediaIsReady() {
        Product product = new Product(1L, 2L, "Phone", "Demo");

        product.publish(true);

        assertEquals(ProductStatus.PUBLISHED, product.status());
    }
}
