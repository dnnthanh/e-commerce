package com.dnnthanh.marketplace.be.cart.api.adapter.out.internal.inventory.rest;

import com.dnnthanh.marketplace.be.cart.api.application.port.out.InventoryAvailabilityPort;
import com.dnnthanh.marketplace.be.cart.api.config.CartClientProperties;
import com.dnnthanh.marketplace.be.cart.api.domain.exception.InvalidCartMutationException;
import com.dnnthanh.marketplace.be.platform.security.ServiceTokenProvider;
import com.dnnthanh.marketplace.be.platform.stereotype.Adapter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Adapter
@RequiredArgsConstructor
public class InventoryAvailabilityRestAdapter implements InventoryAvailabilityPort {

    private final RestClient.Builder restClientBuilder;
    private final ServiceTokenProvider tokens;
    private final CartClientProperties properties;

    @Override
    public InventorySnapshot getBySku(String sku) {
        try {
            InventoryResponse value =
                    restClientBuilder
                            .clone()
                            .baseUrl(properties.getInventory())
                            .build()
                            .get()
                            .uri("/internal/inventory/availability/{sku}", sku)
                            .headers(headers -> headers.setBearerAuth(tokens.token()))
                            .retrieve()
                            .body(InventoryResponse.class);
            if (value == null) {
                throw new InvalidCartMutationException("Inventory not found for SKU " + sku);
            }
            return new InventorySnapshot(sku, value.available());
        } catch (RestClientException failure) {
            throw new InvalidCartMutationException(
                    "Inventory validation unavailable for SKU " + sku);
        }
    }

    private record InventoryResponse(long available) {}
}
