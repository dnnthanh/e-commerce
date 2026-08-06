package com.dnnthanh.marketplace.be.cart.api.adapter.out.internal.catalog.rest;

import com.dnnthanh.marketplace.be.cart.api.application.port.out.CatalogSnapshotPort;
import com.dnnthanh.marketplace.be.cart.api.config.CartClientProperties;
import com.dnnthanh.marketplace.be.cart.api.domain.exception.InvalidCartMutationException;
import com.dnnthanh.marketplace.be.platform.security.ServiceTokenProvider;
import com.dnnthanh.marketplace.be.platform.stereotype.Adapter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Adapter
@RequiredArgsConstructor
public class CatalogSnapshotRestAdapter implements CatalogSnapshotPort {

    private final RestClient.Builder restClientBuilder;
    private final ServiceTokenProvider tokens;
    private final CartClientProperties properties;

    @Override
    public CatalogSnapshot getBySku(String sku) {
        try {
            CatalogResponse value =
                    restClientBuilder
                            .clone()
                            .baseUrl(properties.getCatalog())
                            .build()
                            .get()
                            .uri("/internal/catalog/skus/{sku}", sku)
                            .headers(headers -> headers.setBearerAuth(tokens.token()))
                            .retrieve()
                            .body(CatalogResponse.class);
            if (value == null) {
                throw new InvalidCartMutationException("Catalog SKU not found: " + sku);
            }
            return new CatalogSnapshot(
                    sku, value.sellerId(), value.active(), value.purchaseLimit());
        } catch (RestClientException failure) {
            throw new InvalidCartMutationException("Catalog validation unavailable for SKU " + sku);
        }
    }

    private record CatalogResponse(
            Long skuId, Long productId, Long sellerId, boolean active, int purchaseLimit) {}
}
