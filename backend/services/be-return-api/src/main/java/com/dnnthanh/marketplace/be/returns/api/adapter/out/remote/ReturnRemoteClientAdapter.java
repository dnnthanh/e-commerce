package com.dnnthanh.marketplace.be.returns.api.adapter.out.remote;

import com.dnnthanh.marketplace.be.platform.security.ServiceTokenProvider;
import com.dnnthanh.marketplace.be.platform.stereotype.Adapter;
import com.dnnthanh.marketplace.be.returns.api.adapter.out.remote.exception.ReturnRemoteDependencyException;
import com.dnnthanh.marketplace.be.returns.api.adapter.out.remote.model.PaymentRefundRequest;
import com.dnnthanh.marketplace.be.returns.api.application.port.out.OrderSnapshotPort;
import com.dnnthanh.marketplace.be.returns.api.application.port.out.RefundPaymentPort;
import com.dnnthanh.marketplace.be.returns.api.config.ReturnClientProperties;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Adapter
@RequiredArgsConstructor
public class ReturnRemoteClientAdapter implements OrderSnapshotPort, RefundPaymentPort {

    private final RestClient.Builder restClientBuilder;
    private final ServiceTokenProvider serviceTokenProvider;
    private final ReturnClientProperties properties;

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
                            .headers(headers -> headers.setBearerAuth(serviceTokenProvider.token()))
                            .retrieve()
                            .body(InternalOrder.class);
            return Optional.ofNullable(response).map(ReturnRemoteClientAdapter::toSnapshot);
        } catch (RestClientException exception) {
            throw new ReturnRemoteDependencyException(
                    "Unable to load immutable Order snapshot", exception);
        }
    }

    @Override
    public RefundResult refund(
            String refundKey, String orderId, String returnKey, BigDecimal amount) {
        try {
            PaymentRefundResponse response =
                    restClientBuilder
                            .clone()
                            .baseUrl(properties.getPayment())
                            .build()
                            .post()
                            .uri("/internal/payments/refunds")
                            .headers(headers -> headers.setBearerAuth(serviceTokenProvider.token()))
                            .body(new PaymentRefundRequest(refundKey, orderId, returnKey, amount))
                            .retrieve()
                            .body(PaymentRefundResponse.class);
            if (response == null || response.status() == null) {
                return new RefundResult(RefundStatus.UNKNOWN);
            }
            return new RefundResult(normalizeRefundStatus(response.status()));
        } catch (RestClientException uncertainResult) {
            // A timeout after Payment accepted the stable refund key has an unknown outcome;
            // returning
            // FAILED here could trigger a second financial action instead of reconciliation.
            return new RefundResult(RefundStatus.UNKNOWN);
        }
    }

    private static RefundStatus normalizeRefundStatus(String providerStatus) {
        return switch (providerStatus) {
            case "SUCCEEDED" -> RefundStatus.SUCCEEDED;
            case "FAILED" -> RefundStatus.FAILED;
            default -> RefundStatus.UNKNOWN;
        };
    }

    private static OrderSnapshot toSnapshot(InternalOrder order) {
        return new OrderSnapshot(
                order.orderNo(),
                order.userId(),
                order.status(),
                order.updatedAt(),
                order.sellerOrders().stream()
                        .map(
                                seller ->
                                        new SellerOrderSnapshot(
                                                seller.sellerId(),
                                                seller.lines().stream()
                                                        .map(
                                                                line ->
                                                                        new OrderLineSnapshot(
                                                                                line.orderLineId(),
                                                                                line.sellerId(),
                                                                                line.skuId(),
                                                                                line.quantity(),
                                                                                line.netAmount()))
                                                        .toList()))
                        .toList());
    }

    private record InternalOrder(
            String orderNo,
            String userId,
            BigDecimal grossAmount,
            BigDecimal discountAmount,
            BigDecimal payableAmount,
            String status,
            List<InternalSellerOrder> sellerOrders,
            java.time.LocalDateTime createdAt,
            java.time.LocalDateTime updatedAt,
            long version) {}

    private record InternalSellerOrder(
            Long sellerId,
            String sellerOrderNo,
            BigDecimal grossAmount,
            BigDecimal discountAmount,
            BigDecimal payableAmount,
            String status,
            List<InternalOrderLine> lines) {}

    private record InternalOrderLine(
            Long orderLineId,
            Long sellerId,
            Long skuId,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal allocatedDiscount,
            BigDecimal netAmount) {}

    private record PaymentRefundResponse(String refundKey, String status, BigDecimal amount) {}
}
