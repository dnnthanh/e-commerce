package com.dnnthanh.marketplace.be.fulfillment.api.adapter.out.persistence;

import com.dnnthanh.marketplace.be.fulfillment.api.application.port.out.ShipmentRepositoryPort;
import com.dnnthanh.marketplace.be.fulfillment.api.domain.enumtype.FulfillmentEventType;
import com.dnnthanh.marketplace.be.fulfillment.api.domain.enumtype.ShipmentStatus;
import com.dnnthanh.marketplace.be.fulfillment.api.domain.model.Shipment;
import com.dnnthanh.marketplace.be.fulfillment.api.domain.model.Shipment.ShipmentLine;
import com.dnnthanh.marketplace.be.fulfillment.api.domain.model.Shipment.TrackingEvent;
import com.dnnthanh.marketplace.be.platform.stereotype.Persistence;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

/** SQL Server shipment aggregate adapter with outbox and atomic carrier-event deduplication. */
@Persistence
@RequiredArgsConstructor
public class JdbcShipmentRepositoryAdapter implements ShipmentRepositoryPort {
    private final JdbcClient jdbc;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<Shipment> findByShipmentNo(String shipmentNo) {
        return jdbc.sql(
                        """
                        SELECT id,shipment_no,order_id,seller_id,warehouse_id,carrier_code,tracking_no,
                               status,carrier_sequence
                        FROM shipment WHERE shipment_no=:shipmentNo
                        """)
                .param("shipmentNo", shipmentNo)
                .query((rs, row) -> mapAggregate(rs))
                .optional();
    }

    @Override
    public List<Shipment> findByOrderId(String orderId) {
        return jdbc.sql(
                        """
                        SELECT id,shipment_no,order_id,seller_id,warehouse_id,carrier_code,tracking_no,
                               status,carrier_sequence
                        FROM shipment WHERE order_id=:orderId ORDER BY id
                        """)
                .param("orderId", orderId)
                .query((rs, row) -> mapAggregate(rs))
                .list();
    }

    @Override
    public List<Shipment> findSlaBreaches(LocalDateTime cutoff, int limit) {
        int bounded = Math.max(1, Math.min(limit, 500));
        return jdbc.sql(
                        """
                        SELECT TOP (:limit) id,shipment_no,order_id,seller_id,warehouse_id,carrier_code,
                               tracking_no,status,carrier_sequence
                        FROM shipment
                        WHERE updated_at < :cutoff
                          AND status NOT IN ('DELIVERED','RETURN_TO_SENDER','CANCELLED')
                        ORDER BY updated_at ASC,id ASC
                        """)
                .param("limit", bounded)
                .param("cutoff", cutoff)
                .query((rs, row) -> mapAggregate(rs))
                .list();
    }

    @Override
    @Transactional
    public Shipment save(Shipment shipment) {
        Optional<PersistedShipmentState> existing =
                jdbc.sql(
                                """
                        SELECT id,status,carrier_code,tracking_no
                        FROM shipment WHERE shipment_no=:shipmentNo
                        """)
                        .param("shipmentNo", shipment.shipmentNo())
                        .query(
                                (rs, row) ->
                                        new PersistedShipmentState(
                                                rs.getLong("id"),
                                                ShipmentStatus.valueOf(rs.getString("status")),
                                                rs.getString("carrier_code"),
                                                rs.getString("tracking_no")))
                        .optional();
        Long shipmentId;
        String eventType = null;
        if (existing.isEmpty()) {
            shipmentId =
                    jdbc.sql(
                                    """
                            INSERT INTO shipment(shipment_no,order_id,seller_id,warehouse_id,carrier_code,
                                tracking_no,status,carrier_sequence,created_at,updated_at)
                            OUTPUT INSERTED.id
                            VALUES(:shipmentNo,:orderId,:sellerId,:warehouseId,:carrierCode,:trackingNo,
                                :status,:carrierSequence,SYSDATETIME(),SYSDATETIME())
                            """)
                            .param("shipmentNo", shipment.shipmentNo())
                            .param("orderId", shipment.orderId())
                            .param("sellerId", shipment.sellerId())
                            .param("warehouseId", shipment.warehouseId())
                            .param("carrierCode", shipment.carrierCode())
                            .param("trackingNo", shipment.trackingNo())
                            .param("status", shipment.status().name())
                            .param("carrierSequence", shipment.carrierSequence())
                            .query(Long.class)
                            .single();
            insertLines(shipmentId, shipment.lines());
            eventType = "SHIPMENT_ALLOCATED";
        } else {
            PersistedShipmentState previous = existing.orElseThrow();
            shipmentId = previous.id();
            int updated =
                    jdbc.sql(
                                    """
                            UPDATE shipment
                            SET carrier_code=:carrierCode,tracking_no=:trackingNo,status=:status,
                                carrier_sequence=:carrierSequence,updated_at=SYSDATETIME()
                            WHERE id=:shipmentId
                            """)
                            .param("carrierCode", shipment.carrierCode())
                            .param("trackingNo", shipment.trackingNo())
                            .param("status", shipment.status().name())
                            .param("carrierSequence", shipment.carrierSequence())
                            .param("shipmentId", shipmentId)
                            .update();
            if (updated != 1) {
                throw new ShipmentPersistenceException(
                        "Shipment update lost: " + shipment.shipmentNo());
            }
            if (previous.status() != shipment.status()) {
                eventType = FulfillmentEventType.forShipmentStatus(shipment.status()).name();
            } else if (!java.util.Objects.equals(previous.carrierCode(), shipment.carrierCode())
                    || !java.util.Objects.equals(previous.trackingNo(), shipment.trackingNo())) {
                eventType = "SHIPMENT_CARRIER_ASSIGNED";
            }
        }
        syncTracking(shipmentId, shipment.tracking());
        if (eventType != null) {
            insertOutbox(shipment, eventType);
        }
        return findByShipmentNo(shipment.shipmentNo())
                .orElseThrow(() -> new ShipmentPersistenceException("Shipment cannot be reloaded"));
    }

    @Override
    @Transactional
    public boolean claimCarrierRequest(String idempotencyKey) {
        try {
            jdbc.sql(
                            """
                            INSERT INTO shipment_carrier_request(idempotency_key,processed_at)
                            VALUES(:idempotencyKey,SYSDATETIME())
                            """)
                    .param("idempotencyKey", idempotencyKey)
                    .update();
            return true;
        } catch (DataIntegrityViolationException duplicate) {
            return false;
        }
    }

    private Shipment mapAggregate(ResultSet rs) throws SQLException {
        long shipmentId = rs.getLong("id");
        List<ShipmentLine> lines =
                jdbc.sql(
                                """
                        SELECT order_line_id,sku_id,quantity FROM shipment_item
                        WHERE shipment_id=:shipmentId ORDER BY id
                        """)
                        .param("shipmentId", shipmentId)
                        .query(
                                (lineRs, row) ->
                                        new ShipmentLine(
                                                lineRs.getLong("order_line_id"),
                                                String.valueOf(lineRs.getLong("sku_id")),
                                                lineRs.getInt("quantity")))
                        .list();
        List<TrackingEvent> tracking =
                jdbc.sql(
                                """
                        SELECT provider_sequence,status,occurred_at,source FROM shipment_tracking
                        WHERE shipment_id=:shipmentId ORDER BY provider_sequence
                        """)
                        .param("shipmentId", shipmentId)
                        .query(
                                (trackRs, row) ->
                                        new TrackingEvent(
                                                trackRs.getLong("provider_sequence"),
                                                ShipmentStatus.valueOf(trackRs.getString("status")),
                                                trackRs.getTimestamp("occurred_at")
                                                        .toLocalDateTime(),
                                                trackRs.getString("source")))
                        .list();
        return Shipment.rehydrate(
                rs.getString("shipment_no"),
                rs.getString("order_id"),
                rs.getLong("seller_id"),
                rs.getLong("warehouse_id"),
                lines,
                ShipmentStatus.valueOf(rs.getString("status")),
                rs.getString("carrier_code"),
                rs.getString("tracking_no"),
                rs.getLong("carrier_sequence"),
                tracking);
    }

    private void insertLines(Long shipmentId, List<ShipmentLine> lines) {
        for (ShipmentLine line : lines) {
            jdbc.sql(
                            """
                            INSERT INTO shipment_item(shipment_id,order_line_id,sku_id,quantity)
                            VALUES(:shipmentId,:orderLineId,:skuId,:quantity)
                            """)
                    .param("shipmentId", shipmentId)
                    .param("orderLineId", line.orderLineId())
                    .param("skuId", Long.valueOf(line.sku()))
                    .param("quantity", line.quantity())
                    .update();
        }
    }

    private void syncTracking(Long shipmentId, List<TrackingEvent> tracking) {
        for (TrackingEvent event : tracking) {
            jdbc.sql(
                            """
                            MERGE shipment_tracking AS target
                            USING (SELECT :shipmentId shipment_id,:sequence provider_sequence) AS source
                            ON target.shipment_id=source.shipment_id
                               AND target.provider_sequence=source.provider_sequence
                            WHEN NOT MATCHED THEN
                              INSERT(shipment_id,provider_sequence,status,occurred_at,source)
                              VALUES(:shipmentId,:sequence,:status,:occurredAt,:sourceName);
                            """)
                    .param("shipmentId", shipmentId)
                    .param("sequence", event.sequence())
                    .param("status", event.status().name())
                    .param("occurredAt", event.occurredAt())
                    .param("sourceName", event.source())
                    .update();
        }
    }

    private void insertOutbox(Shipment shipment, String eventType) {
        try {
            String payload =
                    objectMapper.writeValueAsString(
                            Map.of(
                                    "shipmentNo", shipment.shipmentNo(),
                                    "orderId", shipment.orderId(),
                                    "sellerId", shipment.sellerId(),
                                    "status", shipment.status().name()));
            jdbc.sql(
                            """
                            INSERT INTO outbox_event(event_id,aggregate_id,event_type,payload_json,status,created_at)
                            VALUES(:eventId,:aggregateId,:eventType,:payload,'PENDING',SYSDATETIME())
                            """)
                    .param("eventId", UUID.randomUUID().toString())
                    .param("aggregateId", shipment.shipmentNo())
                    .param("eventType", eventType)
                    .param("payload", payload)
                    .update();
        } catch (Exception failure) {
            throw new ShipmentPersistenceException(
                    "Shipment outbox payload cannot be serialized", failure);
        }
    }

    private record PersistedShipmentState(
            Long id, ShipmentStatus status, String carrierCode, String trackingNo) {}

    /** Named infrastructure failure for shipment aggregate persistence. */
    public static final class ShipmentPersistenceException extends RuntimeException {
        public ShipmentPersistenceException(String message) {
            super(message);
        }

        public ShipmentPersistenceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
