package com.dnnthanh.marketplace.be.fulfillment.api.application.service;

import com.dnnthanh.marketplace.be.fulfillment.api.application.port.out.OrderFulfillmentSnapshotPort;
import com.dnnthanh.marketplace.be.fulfillment.api.application.port.out.OrderFulfillmentSnapshotPort.OrderLineSnapshot;
import com.dnnthanh.marketplace.be.fulfillment.api.application.port.out.ShipmentRepositoryPort;
import com.dnnthanh.marketplace.be.fulfillment.api.domain.exception.InvalidShipmentException;
import com.dnnthanh.marketplace.be.fulfillment.api.domain.model.Shipment;
import com.dnnthanh.marketplace.be.fulfillment.api.domain.model.Shipment.ShipmentLine;
import com.dnnthanh.marketplace.be.platform.stereotype.UseCase;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;

/** Allocates a new package while preventing cross-package quantity over-allocation. */
@UseCase
@RequiredArgsConstructor
public class ShipmentAllocationService {
    private final OrderFulfillmentSnapshotPort orderSnapshotPort;
    private final ShipmentRepositoryPort shipmentRepository;

    /**
     * Performs remote Order validation first, then persists locally. No DB connection is held while
     * calling Order.
     */
    public Shipment allocate(AllocationCommand command) {
        Shipment existing = shipmentRepository.findByShipmentNo(command.shipmentNo()).orElse(null);
        if (existing != null) {
            return existing;
        }

        OrderFulfillmentSnapshotPort.OrderSnapshot order =
                orderSnapshotPort
                        .findByOrderNo(command.orderNo())
                        .orElseThrow(
                                () ->
                                        new InvalidShipmentException(
                                                "Order not found: " + command.orderNo()));
        if (!"PAID".equals(order.status()) && !"FULFILLING".equals(order.status())) {
            throw new InvalidShipmentException(
                    "Order is not eligible for fulfillment: " + order.status());
        }

        Map<Long, OrderLineSnapshot> orderLines = new LinkedHashMap<>();
        order.lines().forEach(line -> orderLines.put(line.orderLineId(), line));
        Map<Long, Integer> allocated = currentAllocatedQuantities(command.orderNo());

        List<ShipmentLine> lines =
                command.lines().stream()
                        .map(
                                requested -> {
                                    OrderLineSnapshot source =
                                            orderLines.get(requested.orderLineId());
                                    if (source == null
                                            || !Objects.equals(
                                                    source.sellerId(), command.sellerId())) {
                                        throw new InvalidShipmentException(
                                                "Order line does not belong to seller: "
                                                        + requested.orderLineId());
                                    }
                                    int alreadyAllocated =
                                            allocated.getOrDefault(requested.orderLineId(), 0);
                                    if (alreadyAllocated + requested.quantity()
                                            > source.orderedQuantity()) {
                                        throw new InvalidShipmentException(
                                                "Shipment allocation exceeds ordered quantity for line "
                                                        + requested.orderLineId());
                                    }
                                    allocated.put(
                                            requested.orderLineId(),
                                            alreadyAllocated + requested.quantity());
                                    return new ShipmentLine(
                                            requested.orderLineId(),
                                            String.valueOf(source.skuId()),
                                            requested.quantity());
                                })
                        .toList();

        return shipmentRepository.save(
                new Shipment(
                        command.shipmentNo(),
                        command.orderNo(),
                        command.sellerId(),
                        command.warehouseId(),
                        lines));
    }

    private Map<Long, Integer> currentAllocatedQuantities(String orderNo) {
        Map<Long, Integer> result = new LinkedHashMap<>();
        shipmentRepository.findByOrderId(orderNo).stream()
                .filter(
                        shipment ->
                                shipment.status()
                                        != com.dnnthanh.marketplace.be.fulfillment.api.domain
                                                .enumtype.ShipmentStatus.CANCELLED)
                .flatMap(shipment -> shipment.lines().stream())
                .forEach(line -> result.merge(line.orderLineId(), line.quantity(), Integer::sum));
        return result;
    }

    /** Shipment allocation command from a trusted worker/operations workflow. */
    public record AllocationCommand(
            String shipmentNo,
            String orderNo,
            Long sellerId,
            Long warehouseId,
            List<AllocationLine> lines) {
        public AllocationCommand {
            if (shipmentNo == null
                    || shipmentNo.isBlank()
                    || orderNo == null
                    || orderNo.isBlank()
                    || sellerId == null
                    || warehouseId == null
                    || lines == null
                    || lines.isEmpty()) {
                throw new InvalidShipmentException("Invalid shipment allocation command");
            }
        }
    }

    public record AllocationLine(Long orderLineId, int quantity) {
        public AllocationLine {
            if (orderLineId == null || quantity <= 0) {
                throw new InvalidShipmentException("Invalid shipment allocation line");
            }
        }
    }
}
