package com.dnnthanh.marketplace.be.payment.worker;

import com.dnnthanh.marketplace.be.payment.worker.config.PaymentWorkerClientProperties;
import com.dnnthanh.marketplace.be.platform.event.DomainEvent;
import com.dnnthanh.marketplace.be.platform.kafka.BaseDomainEventConsumer;
import com.dnnthanh.marketplace.be.platform.security.ServiceTokenProvider;
import com.dnnthanh.marketplace.be.platform.stereotype.Adapter;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.client.RestClient;

@Adapter
@RequiredArgsConstructor
public class PaymentRecoveryConsumer extends BaseDomainEventConsumer {

    private final RestClient.Builder restClientBuilder;
    private final ServiceTokenProvider tokenProvider;
    private final PaymentWorkerClientProperties properties;

    @KafkaListener(topics = "marketplace.operations.events", groupId = "payment-recovery-v1")
    public void onRecovery(DomainEvent event) {
        if (!accepts(event, "RECOVERY_REQUESTED")) {
            return;
        }
        if (!"be-payment-worker".equals(String.valueOf(event.payload().get("target")))) {
            return;
        }
        String paymentKey = String.valueOf(event.payload().get("aggregateId"));
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
