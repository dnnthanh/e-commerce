package com.dnnthanh.marketplace.be.checkout.job.adapter.out.internal.checkout.rest;

import com.dnnthanh.marketplace.be.checkout.job.config.CheckoutJobClientProperties;
import com.dnnthanh.marketplace.be.platform.security.ServiceTokenProvider;
import com.dnnthanh.marketplace.be.platform.stereotype.Adapter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestClient;

@Adapter
@RequiredArgsConstructor
public class CheckoutRecoveryRestAdapter {

    private final RestClient.Builder restClientBuilder;
    private final ServiceTokenProvider serviceTokenProvider;
    private final CheckoutJobClientProperties properties;

    public void recover(String checkoutKey) {
        restClientBuilder
                .clone()
                .baseUrl(properties.getCheckout())
                .build()
                .post()
                .uri("/internal/checkout/{checkoutKey}/recover", checkoutKey)
                .headers(headers -> headers.setBearerAuth(serviceTokenProvider.token()))
                .retrieve()
                .toBodilessEntity();
    }
}
