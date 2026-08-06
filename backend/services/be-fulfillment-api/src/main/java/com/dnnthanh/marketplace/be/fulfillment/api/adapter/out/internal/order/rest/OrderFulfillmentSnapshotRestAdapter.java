package com.dnnthanh.marketplace.be.fulfillment.api.adapter.out.internal.order.rest;

import com.dnnthanh.marketplace.be.fulfillment.api.application.port.out.OrderFulfillmentSnapshotPort;
import com.dnnthanh.marketplace.be.fulfillment.api.config.FulfillmentClientProperties;
import com.dnnthanh.marketplace.be.fulfillment.api.domain.exception.InvalidShipmentException;
import com.dnnthanh.marketplace.be.platform.security.ServiceTokenProvider;
import com.dnnthanh.marketplace.be.platform.stereotype.Adapter;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Adapter
@RequiredArgsConstructor
public class OrderFulfillmentSnapshotRestAdapter implements OrderFulfillmentSnapshotPort {
    private final RestClient.Builder restClientBuilder;
    private final ServiceTokenProvider tokens;
    private final FulfillmentClientProperties properties;

    @Override
    public Optional<OrderSnapshot> findByOrderNo(String orderNo) {
        try {
            InternalOrder response =
                    restClientBuilder
                            .clone()
                            .baseUrl(properties.getOrder())
                            .build()
                            .get()
                            .uri("/internal/orders/{orderNo}", orderNo)
                            .headers(headers -> headers.setBearerAuth(tokens.token()))
                            .retrieve()
                            .body(InternalOrder.class);
            if (response == null) {
                return Optional.empty();
            }
            List<OrderLineSnapshot> lines =
                    response.sellerOrders().stream()
                            .flatMap(seller -> seller.lines().stream())
                            .map(
                                    line ->
                                            new OrderLineSnapshot(
                                                    line.orderLineId(),
                                                    line.sellerId(),
                                                    line.skuId(),
                                                    line.quantity()))
                            .toList();
            return Optional.of(new OrderSnapshot(response.orderNo(), response.status(), lines));
        } catch (RestClientException failure) {
            throw new InvalidShipmentException(
                    "Unable to load Order snapshot for fulfillment allocation");
        }
    }

    private record InternalOrder(
            String orderNo, String status, List<InternalSellerOrder> sellerOrders) {}

    private record InternalSellerOrder(Long sellerId, List<InternalOrderLine> lines) {}

    private record InternalOrderLine(Long orderLineId, Long sellerId, Long skuId, int quantity) {}
}
