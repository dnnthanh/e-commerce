package com.dnnthanh.marketplace.be.payment.api.adapter.out.internal.paymentsimulator.rest;

import com.dnnthanh.marketplace.be.payment.api.application.port.out.PaymentProviderPort;
import com.dnnthanh.marketplace.be.payment.api.config.PaymentProviderProperties;
import com.dnnthanh.marketplace.be.payment.api.domain.model.Payment;
import java.math.BigDecimal;
import org.springframework.web.client.RestClient;

/** Shared sandbox protocol implementation; concrete provider adapters select provider semantics. */
abstract class AbstractPaymentSimulatorRestAdapter implements PaymentProviderPort {
    private final RestClient client;
    private final Payment.Provider provider;

    AbstractPaymentSimulatorRestAdapter(
            RestClient.Builder builder,
            PaymentProviderProperties properties,
            Payment.Provider provider) {
        this.client = builder.clone().baseUrl(properties.simulator()).build();
        this.provider = provider;
    }

    @Override
    public Payment.Provider provider() {
        return provider;
    }

    @Override
    public ProviderResult create(Payment payment) {
        return client.post()
                .uri("/sandbox/payments/{provider}", provider.name().toLowerCase())
                .body(
                        new ProviderCommand(
                                payment.paymentKey(), payment.orderId(), payment.amount()))
                .retrieve()
                .body(ProviderResult.class);
    }

    @Override
    public ProviderResult query(Payment payment) {
        return client.get()
                .uri(
                        "/sandbox/payments/{provider}/{key}",
                        provider.name().toLowerCase(),
                        payment.paymentKey())
                .retrieve()
                .body(ProviderResult.class);
    }

    @Override
    public ProviderResult refund(Payment payment, String refundKey, BigDecimal amount) {
        return client.post()
                .uri(
                        "/sandbox/payments/{provider}/{key}/refunds",
                        provider.name().toLowerCase(),
                        payment.paymentKey())
                .body(new RefundCommand(refundKey, amount))
                .retrieve()
                .body(ProviderResult.class);
    }

    private record ProviderCommand(String paymentKey, String orderId, BigDecimal amount) {}

    private record RefundCommand(String refundKey, BigDecimal amount) {}
}
