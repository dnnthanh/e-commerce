package com.dnnthanh.marketplace.be.checkout.api.adapter.out.internal.payment.rest;

import com.dnnthanh.marketplace.be.checkout.api.adapter.out.internal.common.rest.CheckoutInternalRestExchange;
import com.dnnthanh.marketplace.be.checkout.api.adapter.out.internal.common.rest.exception.CheckoutRemoteDependencyException;
import com.dnnthanh.marketplace.be.checkout.api.adapter.out.internal.common.rest.mapper.CheckoutInternalRestMapper;
import com.dnnthanh.marketplace.be.checkout.api.adapter.out.internal.payment.rest.model.PaymentCreateRequest;
import com.dnnthanh.marketplace.be.checkout.api.adapter.out.internal.payment.rest.model.PaymentCreateResponse;
import com.dnnthanh.marketplace.be.checkout.api.application.model.PaymentProvider;
import com.dnnthanh.marketplace.be.checkout.api.application.model.PaymentResult;
import com.dnnthanh.marketplace.be.checkout.api.application.port.out.PaymentClientPort;
import com.dnnthanh.marketplace.be.checkout.api.config.CheckoutRemoteProperties;
import com.dnnthanh.marketplace.be.checkout.api.config.CheckoutRemoteResilienceNames;
import com.dnnthanh.marketplace.be.platform.stereotype.Adapter;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestClient;

@Adapter
@RequiredArgsConstructor
public class PaymentRestAdapter implements PaymentClientPort {
    private final RestClient.Builder restClientBuilder;
    private final CheckoutRemoteProperties properties;
    private final CheckoutInternalRestExchange exchange;
    private final CheckoutInternalRestMapper mapper;

    @Override
    public PaymentResult create(
            String paymentKey,
            String orderNo,
            String userId,
            PaymentProvider provider,
            BigDecimal amount) {
        PaymentCreateRequest request =
                mapper.toPaymentCreateRequest(paymentKey, orderNo, userId, provider, amount);
        PaymentCreateResponse response =
                exchange.post(
                        CheckoutRemoteResilienceNames.PAYMENT,
                        client(),
                        "/internal/payments",
                        request,
                        PaymentCreateResponse.class);
        if (response == null || response.status() == null) {
            throw new CheckoutRemoteDependencyException(
                    "Payment service returned no payment status");
        }
        return mapper.toPaymentResult(response);
    }

    private RestClient client() {
        return restClientBuilder.clone().baseUrl(properties.getPaymentBaseUrl()).build();
    }
}
