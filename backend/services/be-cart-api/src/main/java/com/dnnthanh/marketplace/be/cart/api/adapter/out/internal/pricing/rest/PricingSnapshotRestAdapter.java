package com.dnnthanh.marketplace.be.cart.api.adapter.out.internal.pricing.rest;

import com.dnnthanh.marketplace.be.cart.api.application.port.out.PricingSnapshotPort;
import com.dnnthanh.marketplace.be.cart.api.config.CartClientProperties;
import com.dnnthanh.marketplace.be.cart.api.domain.exception.InvalidCartMutationException;
import com.dnnthanh.marketplace.be.platform.security.ServiceTokenProvider;
import com.dnnthanh.marketplace.be.platform.stereotype.Adapter;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Adapter
@RequiredArgsConstructor
public class PricingSnapshotRestAdapter implements PricingSnapshotPort {

    private final RestClient.Builder restClientBuilder;
    private final ServiceTokenProvider tokens;
    private final CartClientProperties properties;

    @Override
    public PriceSnapshot quote(String sku, Long sellerId, String channel) {
        try {
            PriceResponse value =
                    restClientBuilder
                            .clone()
                            .baseUrl(properties.getPricing())
                            .build()
                            .get()
                            .uri(
                                    "/internal/pricing/quote?sku={sku}&sellerId={sellerId}&channel={channel}",
                                    sku,
                                    sellerId,
                                    channel)
                            .headers(headers -> headers.setBearerAuth(tokens.token()))
                            .retrieve()
                            .body(PriceResponse.class);
            if (value == null) {
                throw new InvalidCartMutationException("Price not found for SKU " + sku);
            }
            return new PriceSnapshot(sku, sellerId, value.amount(), value.currency());
        } catch (RestClientException failure) {
            throw new InvalidCartMutationException("Pricing validation unavailable for SKU " + sku);
        }
    }

    private record PriceResponse(BigDecimal amount, String currency) {}
}
