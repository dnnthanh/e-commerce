package com.dnnthanh.marketplace.be.payment.job.adapter.out.internal.payment.rest;

import com.dnnthanh.marketplace.be.payment.job.config.PaymentJobClientProperties;
import com.dnnthanh.marketplace.be.platform.security.ServiceTokenProvider;
import com.dnnthanh.marketplace.be.platform.stereotype.Adapter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestClient;

@Adapter
@RequiredArgsConstructor
public class PaymentReconciliationRestAdapter {

    private final RestClient.Builder restClientBuilder;
    private final ServiceTokenProvider tokenProvider;
    private final PaymentJobClientProperties properties;

    public void reconcile(String paymentKey) {
        restClientBuilder
                .clone()
                .baseUrl(properties.getPayment())
                .build()
                .post()
                .uri("/internal/payments/{paymentKey}/reconcile", paymentKey)
                .headers(headers -> headers.setBearerAuth(tokenProvider.token()))
                .retrieve()
                .toBodilessEntity();
    }
}
