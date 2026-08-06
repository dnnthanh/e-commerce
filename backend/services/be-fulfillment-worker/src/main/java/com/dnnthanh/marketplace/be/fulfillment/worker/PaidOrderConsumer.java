package com.dnnthanh.marketplace.be.fulfillment.worker;

import com.dnnthanh.marketplace.be.fulfillment.worker.config.FulfillmentWorkerClientProperties;
import com.dnnthanh.marketplace.be.platform.event.DomainEvent;
import com.dnnthanh.marketplace.be.platform.kafka.BaseDomainEventConsumer;
import com.dnnthanh.marketplace.be.platform.security.ServiceTokenProvider;
import com.dnnthanh.marketplace.be.platform.stereotype.Adapter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.client.RestClient;

/**
 * Fetches Order/Inventory snapshots after payment succeeds and delegates local writes
 * transactionally.
 */
@Adapter
@RequiredArgsConstructor
public class PaidOrderConsumer extends BaseDomainEventConsumer {

    private final RestClient.Builder restClientBuilder;
    private final ServiceTokenProvider tokens;
    private final ShipmentMaterializer materializer;
    private final FulfillmentWorkerClientProperties properties;

    @KafkaListener(topics = "marketplace.payment.events", groupId = "fulfillment-payment-v2")
    public void paid(DomainEvent event) {
        if (!accepts(event, "PAYMENT_SUCCEEDED")) {
            return;
        }

        String orderNo = requiredString(event, "orderId");
        ShipmentMaterializer.OrderLine[] lines =
                get(
                        properties.getOrder() + "/internal/orders/{orderNo}/lines",
                        ShipmentMaterializer.OrderLine[].class,
                        orderNo);
        ShipmentMaterializer.Reservation[] reservations =
                get(
                        properties.getInventory()
                                + "/internal/inventory/orders/{orderNo}/reservations",
                        ShipmentMaterializer.Reservation[].class,
                        orderNo);

        materializer.materialize(
                event.eventId(),
                orderNo,
                lines == null ? List.of() : List.of(lines),
                reservations == null ? List.of() : List.of(reservations));
    }

    private <T> T get(String uri, Class<T> responseType, Object... uriVariables) {
        return restClientBuilder
                .build()
                .get()
                .uri(uri, uriVariables)
                .headers(headers -> headers.setBearerAuth(tokens.token()))
                .retrieve()
                .body(responseType);
    }
}
