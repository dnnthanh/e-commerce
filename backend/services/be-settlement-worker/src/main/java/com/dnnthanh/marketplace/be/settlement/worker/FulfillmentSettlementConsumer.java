package com.dnnthanh.marketplace.be.settlement.worker;

import com.dnnthanh.marketplace.be.platform.event.DomainEvent;
import com.dnnthanh.marketplace.be.platform.kafka.BaseDomainEventConsumer;
import com.dnnthanh.marketplace.be.platform.security.ServiceTokenProvider;
import com.dnnthanh.marketplace.be.platform.stereotype.Adapter;
import com.dnnthanh.marketplace.be.settlement.worker.config.SettlementWorkerClientProperties;
import com.dnnthanh.marketplace.be.settlement.worker.exception.SettlementMaterializationException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.client.RestClient;

/**
 * Resolves the authoritative seller-order snapshot before entering the local Oracle transaction.
 */
@Adapter
@RequiredArgsConstructor
public class FulfillmentSettlementConsumer extends BaseDomainEventConsumer {

    private final RestClient.Builder restClientBuilder;
    private final ServiceTokenProvider serviceTokenProvider;
    private final SettlementMaterializer materializer;
    private final SettlementWorkerClientProperties properties;

    @KafkaListener(topics = "marketplace.fulfillment.events", groupId = "settlement-fulfillment-v2")
    public void onFulfillment(DomainEvent event) {
        if (!accepts(event, "SELLER_FULFILLMENT_COMPLETED")) {
            return;
        }

        String orderNo = requiredString(event, "orderId");
        Long sellerId = requiredLong(event, "sellerId");
        InternalOrder order =
                restClientBuilder
                        .clone()
                        .baseUrl(properties.getOrder())
                        .build()
                        .get()
                        .uri("/internal/orders/{orderNo}", orderNo)
                        .headers(headers -> headers.setBearerAuth(serviceTokenProvider.token()))
                        .retrieve()
                        .body(InternalOrder.class);
        if (order == null) {
            throw new SettlementMaterializationException(
                    "Order snapshot was not returned for " + orderNo);
        }

        SettlementMaterializer.SellerOrderSnapshot sellerOrder =
                order.sellerOrders().stream()
                        .filter(candidate -> sellerId.equals(candidate.sellerId()))
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new SettlementMaterializationException(
                                                "Seller order was not found for order="
                                                        + orderNo
                                                        + ", seller="
                                                        + sellerId));

        materializer.materialize(event.eventId(), sellerOrder);
    }

    private record InternalOrder(
            String orderNo,
            String userId,
            BigDecimal grossAmount,
            BigDecimal discountAmount,
            BigDecimal payableAmount,
            String status,
            List<SettlementMaterializer.SellerOrderSnapshot> sellerOrders,
            LocalDateTime createdAt) {}
}
