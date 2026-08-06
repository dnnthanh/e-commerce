package com.dnnthanh.marketplace.be.returns.api.application.port.out;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Anti-corruption port to immutable Order snapshots required by return creation. */
public interface OrderSnapshotPort {
    Optional<OrderSnapshot> findByOrderNo(String orderNo);

    record OrderSnapshot(
            String orderNo,
            String userId,
            String status,
            LocalDateTime deliveredAt,
            List<SellerOrderSnapshot> sellerOrders) {}

    record SellerOrderSnapshot(Long sellerId, List<OrderLineSnapshot> lines) {}

    record OrderLineSnapshot(
            Long orderLineId, Long sellerId, Long skuId, int quantity, BigDecimal netAmount) {
        public BigDecimal refundableUnitAmount() {
            return netAmount.divide(
                    BigDecimal.valueOf(quantity), 2, java.math.RoundingMode.HALF_UP);
        }
    }
}
