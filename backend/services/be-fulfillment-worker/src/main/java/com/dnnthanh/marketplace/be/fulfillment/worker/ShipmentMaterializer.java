package com.dnnthanh.marketplace.be.fulfillment.worker;

import com.dnnthanh.marketplace.be.fulfillment.worker.exception.FulfillmentMaterializationException;
import com.dnnthanh.marketplace.be.platform.stereotype.Persistence;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;

@Persistence
@RequiredArgsConstructor
public class ShipmentMaterializer {

    private final JdbcClient jdbc;

    @Transactional
    public void materialize(
            String eventId, String orderNo, List<OrderLine> lines, List<Reservation> reservations) {
        if (!claim(eventId)) {
            return;
        }

        Map<Long, Reservation> reservationBySku = new HashMap<>();
        for (Reservation reservation : reservations) {
            reservationBySku.put(reservation.skuId(), reservation);
        }

        Map<ShipmentGroup, List<OrderLine>> groupedLines = new LinkedHashMap<>();
        for (OrderLine line : lines) {
            Reservation reservation = reservationBySku.get(line.skuId());
            if (reservation == null) {
                throw new FulfillmentMaterializationException(
                        "Reservation placement missing for sku " + line.skuId());
            }
            ShipmentGroup group = new ShipmentGroup(line.sellerId(), reservation.warehouseId());
            groupedLines.computeIfAbsent(group, ignored -> new ArrayList<>()).add(line);
        }

        for (Map.Entry<ShipmentGroup, List<OrderLine>> entry : groupedLines.entrySet()) {
            createShipmentIfMissing(orderNo, entry.getKey(), entry.getValue());
        }
    }

    private void createShipmentIfMissing(
            String orderNo, ShipmentGroup group, List<OrderLine> lines) {
        String shipmentNo = "SHP-" + orderNo + "-" + group.sellerId() + "-" + group.warehouseId();
        Long shipmentId =
                jdbc.sql("SELECT id FROM shipment WHERE shipment_no=:shipmentNo")
                        .param("shipmentNo", shipmentNo)
                        .query(Long.class)
                        .optional()
                        .orElse(null);
        if (shipmentId != null) {
            return;
        }

        shipmentId =
                jdbc.sql(
                                """
                                INSERT INTO shipment(
                                    shipment_no, order_id, seller_id, warehouse_id,
                                    status, created_at, updated_at)
                                VALUES(
                                    :shipmentNo, :orderNo, :sellerId, :warehouseId,
                                    'ALLOCATED', SYSDATETIME(), SYSDATETIME());
                                SELECT CAST(SCOPE_IDENTITY() AS BIGINT)
                                """)
                        .param("shipmentNo", shipmentNo)
                        .param("orderNo", orderNo)
                        .param("sellerId", group.sellerId())
                        .param("warehouseId", group.warehouseId())
                        .query(Long.class)
                        .single();

        for (OrderLine line : lines) {
            jdbc.sql(
                            """
                            INSERT INTO shipment_item(shipment_id, order_line_id, sku_id, quantity)
                            VALUES(:shipmentId, :orderLineId, :skuId, :quantity)
                            """)
                    .param("shipmentId", shipmentId)
                    .param("orderLineId", line.orderLineId())
                    .param("skuId", line.skuId())
                    .param("quantity", line.quantity())
                    .update();
        }
    }

    private boolean claim(String eventId) {
        try {
            jdbc.sql(
                            """
                            INSERT INTO inbox_event(consumer_name, event_id, processed_at)
                            VALUES('fulfillment-payment-v2', :eventId, :processedAt)
                            """)
                    .param("eventId", eventId)
                    .param("processedAt", LocalDateTime.now())
                    .update();
            return true;
        } catch (DuplicateKeyException duplicate) {
            return false;
        }
    }

    public record OrderLine(
            Long orderLineId,
            Long sellerId,
            Long skuId,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal allocatedDiscount,
            BigDecimal netAmount) {}

    public record Reservation(
            String reservationKey, Long skuId, Long warehouseId, long quantity, String status) {}

    private record ShipmentGroup(Long sellerId, Long warehouseId) {}
}
