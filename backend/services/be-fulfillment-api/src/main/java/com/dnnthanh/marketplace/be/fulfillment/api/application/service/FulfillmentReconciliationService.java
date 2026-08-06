package com.dnnthanh.marketplace.be.fulfillment.api.application.service;

import com.dnnthanh.marketplace.be.fulfillment.api.application.port.out.OrderFulfillmentSnapshotPort;
import com.dnnthanh.marketplace.be.fulfillment.api.application.port.out.ShipmentRepositoryPort;
import com.dnnthanh.marketplace.be.fulfillment.api.domain.enumtype.ShipmentStatus;
import com.dnnthanh.marketplace.be.fulfillment.api.domain.exception.InvalidShipmentException;
import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

/** Reconciles immutable Order quantities with allocated and delivered package quantities. */
@UseCase
@RequiredArgsConstructor
public class FulfillmentReconciliationService {
    private final OrderFulfillmentSnapshotPort orderSnapshotPort;
    private final ShipmentRepositoryPort shipmentRepository;

    public List<LineReconciliation> reconcile(String orderNo) {
        OrderFulfillmentSnapshotPort.OrderSnapshot order =
                orderSnapshotPort
                        .findByOrderNo(orderNo)
                        .orElseThrow(
                                () -> new InvalidShipmentException("Order not found: " + orderNo));
        Map<Long, Integer> allocated = new LinkedHashMap<>();
        Map<Long, Integer> delivered = new LinkedHashMap<>();
        shipmentRepository
                .findByOrderId(orderNo)
                .forEach(
                        shipment ->
                                shipment.lines()
                                        .forEach(
                                                line -> {
                                                    if (shipment.status()
                                                            != ShipmentStatus.CANCELLED) {
                                                        allocated.merge(
                                                                line.orderLineId(),
                                                                line.quantity(),
                                                                Integer::sum);
                                                    }
                                                    if (shipment.status()
                                                            == ShipmentStatus.DELIVERED) {
                                                        delivered.merge(
                                                                line.orderLineId(),
                                                                line.quantity(),
                                                                Integer::sum);
                                                    }
                                                }));
        return order.lines().stream()
                .map(
                        line ->
                                new LineReconciliation(
                                        line.orderLineId(),
                                        line.orderedQuantity(),
                                        allocated.getOrDefault(line.orderLineId(), 0),
                                        delivered.getOrDefault(line.orderLineId(), 0)))
                .toList();
    }

    public record LineReconciliation(
            Long orderLineId, int orderedQuantity, int allocatedQuantity, int deliveredQuantity) {
        public int allocationDrift() {
            return allocatedQuantity - orderedQuantity;
        }

        public int deliveryRemaining() {
            return orderedQuantity - deliveredQuantity;
        }
    }
}
