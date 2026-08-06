package com.dnnthanh.marketplace.be.checkout.api.adapter.out.internal.pricing.rest;

import com.dnnthanh.marketplace.be.checkout.api.adapter.out.internal.common.rest.CheckoutInternalRestExchange;
import com.dnnthanh.marketplace.be.checkout.api.adapter.out.internal.common.rest.exception.CheckoutRemoteDependencyException;
import com.dnnthanh.marketplace.be.checkout.api.adapter.out.internal.pricing.rest.model.PriceResponse;
import com.dnnthanh.marketplace.be.checkout.api.application.port.out.PricingClientPort;
import com.dnnthanh.marketplace.be.checkout.api.config.CheckoutRemoteProperties;
import com.dnnthanh.marketplace.be.checkout.api.config.CheckoutRemoteResilienceNames;
import com.dnnthanh.marketplace.be.platform.stereotype.Adapter;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestClient;

@Adapter
@RequiredArgsConstructor
public class PricingRestAdapter implements PricingClientPort {
    private final RestClient.Builder restClientBuilder;
    private final CheckoutRemoteProperties properties;
    private final CheckoutInternalRestExchange exchange;

    @Override
    public BigDecimal price(Long skuId) {
        PriceResponse response =
                exchange.get(
                        CheckoutRemoteResilienceNames.PRICING,
                        client(),
                        "/prices?skuId={sku}",
                        PriceResponse.class,
                        skuId);
        if (response == null || response.amount() == null) {
            throw new CheckoutRemoteDependencyException("Pricing service returned no amount");
        }
        return response.amount();
    }

    private RestClient client() {
        return restClientBuilder.clone().baseUrl(properties.getPricingBaseUrl()).build();
    }
}
