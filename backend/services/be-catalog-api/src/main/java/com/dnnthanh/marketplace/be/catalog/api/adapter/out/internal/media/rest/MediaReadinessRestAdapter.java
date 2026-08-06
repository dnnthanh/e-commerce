package com.dnnthanh.marketplace.be.catalog.api.adapter.out.internal.media.rest;

import com.dnnthanh.marketplace.be.catalog.api.application.port.out.MediaReadinessPort;
import com.dnnthanh.marketplace.be.catalog.api.config.CatalogMediaProperties;
import com.dnnthanh.marketplace.be.platform.stereotype.Adapter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestClient;

@Adapter
@RequiredArgsConstructor
public class MediaReadinessRestAdapter implements MediaReadinessPort {

    private final RestClient.Builder restClientBuilder;
    private final CatalogMediaProperties properties;

    @Override
    public boolean isReady(Long productId) {
        Readiness response =
                restClientBuilder
                        .clone()
                        .baseUrl(properties.getBaseUrl())
                        .build()
                        .get()
                        .uri("/internal/media/products/{id}/readiness", productId)
                        .retrieve()
                        .body(Readiness.class);
        return response != null && response.ready();
    }

    private record Readiness(boolean ready) {}
}
