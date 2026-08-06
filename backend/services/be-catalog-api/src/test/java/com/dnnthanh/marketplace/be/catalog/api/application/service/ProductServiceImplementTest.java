package com.dnnthanh.marketplace.be.catalog.api.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.dnnthanh.marketplace.be.catalog.api.application.dto.ProductOffer;
import com.dnnthanh.marketplace.be.catalog.api.application.exception.ProductNotFoundException;
import com.dnnthanh.marketplace.be.catalog.api.application.port.out.MediaReadinessPort;
import com.dnnthanh.marketplace.be.catalog.api.application.port.out.ProductOfferPort;
import com.dnnthanh.marketplace.be.catalog.api.application.port.out.ProductRepositoryPort;
import com.dnnthanh.marketplace.be.catalog.api.domain.model.Product;
import com.dnnthanh.marketplace.be.catalog.api.domain.model.ProductStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplementTest {

    @Mock private ProductRepositoryPort repository;
    @Mock private MediaReadinessPort mediaReadiness;
    @Mock private ProductOfferPort offerPort;

    private ProductServiceImplement service;

    @BeforeEach
    void setUp() {
        service = new ProductServiceImplement(repository, mediaReadiness, offerPort);
    }

    @Test
    void offersReturnsProjectionAfterPublishedProductVisibilityCheck() {
        Product published = publishedProduct();
        ProductOffer offer = new ProductOffer(100L, 10L, 20L, "Phone", "PHONE-BLACK", "Black", 5);
        when(repository.findById(10L)).thenReturn(Optional.of(published));
        when(offerPort.findSellableByProductId(10L)).thenReturn(List.of(offer));

        assertThat(service.offers(10L)).containsExactly(offer);
        verify(offerPort).findSellableByProductId(10L);
    }

    @Test
    void offersDoesNotLeakDraftProduct() {
        Product draft =
                Product.rehydrate(
                        10L,
                        20L,
                        30L,
                        "Phone",
                        "desc",
                        ProductStatus.DRAFT,
                        0L,
                        LocalDateTime.now(),
                        LocalDateTime.now());
        when(repository.findById(10L)).thenReturn(Optional.of(draft));

        assertThatThrownBy(() -> service.offers(10L)).isInstanceOf(ProductNotFoundException.class);
        verifyNoInteractions(offerPort);
    }

    @Test
    void offerDoesNotLeakMissingOrNonSellableSku() {
        when(offerPort.findSellableBySkuId(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.offer(100L)).isInstanceOf(ProductNotFoundException.class);
    }

    private Product publishedProduct() {
        return Product.rehydrate(
                10L,
                20L,
                30L,
                "Phone",
                "desc",
                ProductStatus.PUBLISHED,
                0L,
                LocalDateTime.now(),
                LocalDateTime.now());
    }
}
