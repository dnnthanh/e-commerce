package com.dnnthanh.marketplace.be.fulfillment.api.application.port.out;

import java.util.List;
import java.util.Optional;

/** Anti-corruption port to immutable Order line quantities used for package allocation. */
public interface OrderFulfillmentSnapshotPort {
    Optional<OrderSnapshot> findByOrderNo(String orderNo);

    record OrderSnapshot(String orderNo, String status, List<OrderLineSnapshot> lines) {}

    record OrderLineSnapshot(Long orderLineId, Long sellerId, Long skuId, int orderedQuantity) {}
}
