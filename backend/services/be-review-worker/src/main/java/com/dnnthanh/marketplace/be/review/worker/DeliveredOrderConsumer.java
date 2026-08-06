package com.dnnthanh.marketplace.be.review.worker;

import com.dnnthanh.marketplace.be.platform.event.DomainEvent;
import com.dnnthanh.marketplace.be.platform.kafka.BaseDomainEventConsumer;
import com.dnnthanh.marketplace.be.platform.security.ServiceTokenProvider;
import com.dnnthanh.marketplace.be.platform.stereotype.Adapter;
import com.dnnthanh.marketplace.be.review.worker.config.ReviewWorkerClientProperties;
import com.dnnthanh.marketplace.be.review.worker.exception.ReviewMaterializationException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.client.RestClient;

/**
 * Builds the Review verified-purchase projection after a seller order is actually delivered. All
 * remote enrichment happens before the local Review database transaction starts.
 */
@Adapter
@RequiredArgsConstructor
public class DeliveredOrderConsumer extends BaseDomainEventConsumer {

    private final RestClient.Builder restClientBuilder;
    private final ServiceTokenProvider serviceTokenProvider;
    private final VerifiedPurchaseMaterializer materializer;
    private final ReviewWorkerClientProperties properties;

    @KafkaListener(topics = "marketplace.fulfillment.events", groupId = "review-fulfillment-v3")
    public void delivered(DomainEvent event) {
        if (!accepts(event, "SELLER_FULFILLMENT_COMPLETED")) {
            return;
        }

        String orderNo = requiredString(event, "orderId");
        Long sellerId = requiredLong(event, "sellerId");
        InternalOrder order =
                get(
                        properties.getOrder() + "/internal/orders/{orderNo}",
                        InternalOrder.class,
                        orderNo);

        InternalSellerOrder sellerOrder =
                order.sellerOrders().stream()
                        .filter(candidate -> sellerId.equals(candidate.sellerId()))
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new ReviewMaterializationException(
                                                "Seller order was not found for order="
                                                        + orderNo
                                                        + ", seller="
                                                        + sellerId));

        List<VerifiedPurchaseLine> verifiedLines = new ArrayList<>(sellerOrder.lines().size());
        for (OrderLine line : sellerOrder.lines()) {
            SkuOwner owner =
                    get(
                            properties.getCatalog() + "/internal/catalog/skus/{skuId}",
                            SkuOwner.class,
                            line.skuId());
            if (owner.productId() == null) {
                throw new ReviewMaterializationException(
                        "Catalog could not resolve SKU " + line.skuId());
            }
            verifiedLines.add(
                    new VerifiedPurchaseLine(line.orderLineId(), owner.productId(), line.skuId()));
        }

        materializer.materialize(event.eventId(), order.userId(), List.copyOf(verifiedLines));
    }

    private <T> T get(String uri, Class<T> responseType, Object... uriVariables) {
        T response =
                restClientBuilder
                        .build()
                        .get()
                        .uri(uri, uriVariables)
                        .headers(headers -> headers.setBearerAuth(serviceTokenProvider.token()))
                        .retrieve()
                        .body(responseType);
        if (response == null) {
            throw new ReviewMaterializationException(
                    "Internal service returned an empty response for " + uri);
        }
        return response;
    }

    private record InternalOrder(
            String orderNo,
            String userId,
            BigDecimal grossAmount,
            BigDecimal discountAmount,
            BigDecimal payableAmount,
            String status,
            List<InternalSellerOrder> sellerOrders,
            LocalDateTime createdAt) {}

    private record InternalSellerOrder(
            Long sellerId,
            String sellerOrderNo,
            BigDecimal grossAmount,
            BigDecimal discountAmount,
            BigDecimal payableAmount,
            String status,
            List<OrderLine> lines) {}

    private record OrderLine(
            Long orderLineId,
            Long sellerId,
            Long skuId,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal allocatedDiscount,
            BigDecimal netAmount) {}

    private record SkuOwner(Long skuId, Long productId, Long sellerId) {}
}
