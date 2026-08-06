package com.dnnthanh.marketplace.be.inventory.api.adapter.out.persistence;

import com.dnnthanh.marketplace.be.inventory.api.application.exception.ReservationAttachmentException;
import com.dnnthanh.marketplace.be.inventory.api.application.port.out.InventoryOperationsPort;
import com.dnnthanh.marketplace.be.inventory.api.application.port.out.InventoryRepositoryPort;
import com.dnnthanh.marketplace.be.inventory.api.application.port.out.ReservationLifecyclePort;
import com.dnnthanh.marketplace.be.inventory.api.application.port.out.StockLedgerPort;
import com.dnnthanh.marketplace.be.inventory.api.application.query.OrderReservationQueryResult;
import com.dnnthanh.marketplace.be.inventory.api.domain.enumtype.ReservationStatus;
import com.dnnthanh.marketplace.be.inventory.api.domain.exception.InvalidInventoryMutationException;
import com.dnnthanh.marketplace.be.inventory.api.domain.exception.ReservationIdempotencyConflictException;
import com.dnnthanh.marketplace.be.inventory.api.domain.model.InventoryBalance;
import com.dnnthanh.marketplace.be.inventory.api.domain.model.Reservation;
import com.dnnthanh.marketplace.be.inventory.api.domain.model.StockLedger;
import com.dnnthanh.marketplace.be.platform.stereotype.Persistence;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;

/**
 * PostgreSQL inventory adapter.
 *
 * <p>JDBC is intentional here: inventory reservation is a contention-sensitive operation whose
 * correctness depends on conditional SQL updates and row locks. Ordinary application services do
 * not own SQL.
 */
@Persistence
@RequiredArgsConstructor
public class JdbcInventoryRepositoryAdapter
        implements InventoryRepositoryPort,
                ReservationLifecyclePort,
                StockLedgerPort,
                InventoryOperationsPort {

    private final JdbcClient jdbc;

    @Override
    public Optional<Reservation> findReservation(String key) {
        return jdbc.sql(
                        """
            SELECT reservation_key, sku_id, warehouse_id, quantity, status, expires_at
            FROM inventory_reservation
            WHERE reservation_key = :key
            """)
                .param("key", key)
                .query(
                        (rs, row) ->
                                new Reservation(
                                        rs.getString("reservation_key"),
                                        rs.getLong("sku_id"),
                                        rs.getLong("warehouse_id"),
                                        rs.getLong("quantity"),
                                        ReservationStatus.valueOf(rs.getString("status")),
                                        rs.getTimestamp("expires_at").toLocalDateTime()))
                .optional();
    }

    @Override
    public Optional<InventoryBalance> findBalance(Long skuId, Long warehouseId) {
        return jdbc.sql(
                        """
            SELECT sku_id, warehouse_id, on_hand, reserved, version
            FROM inventory_balance
            WHERE sku_id = :sku AND warehouse_id = :warehouse
            """)
                .param("sku", skuId)
                .param("warehouse", warehouseId)
                .query(
                        (rs, row) ->
                                new InventoryBalance(
                                        rs.getLong("sku_id"),
                                        rs.getLong("warehouse_id"),
                                        rs.getLong("on_hand"),
                                        rs.getLong("reserved"),
                                        rs.getLong("version")))
                .optional();
    }

    @Override
    public boolean updateBalance(InventoryBalance balance) {
        return jdbc.sql(
                                """
                UPDATE inventory_balance
                SET reserved = :reserved, version = version + 1
                WHERE sku_id = :sku
                  AND warehouse_id = :warehouse
                  AND version = :version
                  AND on_hand >= :reserved
                """)
                        .param("reserved", balance.reserved())
                        .param("sku", balance.skuId())
                        .param("warehouse", balance.warehouseId())
                        .param("version", balance.version())
                        .update()
                == 1;
    }

    @Override
    public void insertReservation(Reservation reservation) {
        jdbc.sql(
                        """
            INSERT INTO inventory_reservation(
                reservation_key, sku_id, warehouse_id, quantity, status, expires_at,
                created_at, updated_at)
            VALUES(
                :key, :sku, :warehouse, :quantity, :status, :expires,
                now(), now())
            """)
                .param("key", reservation.reservationKey())
                .param("sku", reservation.skuId())
                .param("warehouse", reservation.warehouseId())
                .param("quantity", reservation.quantity())
                .param("status", reservation.status().name())
                .param("expires", reservation.expiresAt())
                .update();
    }

    @Override
    public int expireReservations(LocalDateTime now) {
        return jdbc.sql(
                        """
            UPDATE inventory_reservation
            SET status = 'EXPIRED', updated_at = :now
            WHERE status = 'RESERVED' AND expires_at < :now
            """)
                .param("now", now)
                .update();
    }

    @Override
    @Transactional
    public void attachOrder(List<String> reservationKeys, String orderId) {
        for (String reservationKey : reservationKeys) {
            int changed =
                    jdbc.sql(
                                    """
                  UPDATE inventory_reservation
                  SET order_id = :orderId, updated_at = now()
                  WHERE reservation_key = :reservationKey
                    AND (order_id IS NULL OR order_id = :orderId)
                  """)
                            .param("orderId", orderId)
                            .param("reservationKey", reservationKey)
                            .update();
            if (changed != 1) {
                throw new ReservationAttachmentException(reservationKey, orderId);
            }
        }
    }

    @Override
    @Transactional
    public void releaseReservations(List<String> reservationKeys) {
        for (String reservationKey : reservationKeys) {
            var row =
                    jdbc.sql(
                                    """
                  SELECT id, sku_id, warehouse_id, quantity, status
                  FROM inventory_reservation
                  WHERE reservation_key = :reservationKey
                  FOR UPDATE
                  """)
                            .param("reservationKey", reservationKey)
                            .query(new ColumnMapRowMapper())
                            .optional()
                            .orElse(null);
            if (row == null
                    || ReservationStatus.valueOf(String.valueOf(row.get("status")))
                            != ReservationStatus.RESERVED) {
                continue;
            }

            int balanceChanged =
                    jdbc.sql(
                                    """
                  UPDATE inventory_balance
                  SET reserved = reserved - :quantity, version = version + 1
                  WHERE sku_id = :sku
                    AND warehouse_id = :warehouse
                    AND reserved >= :quantity
                  """)
                            .param("quantity", row.get("quantity"))
                            .param("sku", row.get("sku_id"))
                            .param("warehouse", row.get("warehouse_id"))
                            .update();
            if (balanceChanged != 1) {
                throw new InvalidInventoryMutationException(
                        "Reservation release would violate inventory balance");
            }

            jdbc.sql(
                            """
              UPDATE inventory_reservation
              SET status = 'RELEASED', updated_at = now()
              WHERE id = :id
              """)
                    .param("id", row.get("id"))
                    .update();
        }
    }

    @Override
    public List<OrderReservationQueryResult> findByOrder(String orderId) {
        return jdbc.sql(
                        """
            SELECT reservation_key, sku_id, warehouse_id, quantity, status
            FROM inventory_reservation
            WHERE order_id = :orderId
            ORDER BY id
            """)
                .param("orderId", orderId)
                .query(
                        (rs, row) ->
                                new OrderReservationQueryResult(
                                        rs.getString("reservation_key"),
                                        rs.getLong("sku_id"),
                                        rs.getLong("warehouse_id"),
                                        rs.getLong("quantity"),
                                        ReservationStatus.valueOf(rs.getString("status"))))
                .list();
    }

    @Override
    public Optional<StockLedger> find(String sku, Long warehouseId) {
        Long skuId = skuId(sku);
        return findBalance(skuId, warehouseId)
                .map(
                        balance ->
                                StockLedger.rehydrate(
                                        sku, warehouseId, balance.onHand(), balance.reserved()));
    }

    @Override
    @Transactional
    public StockLedger save(StockLedger ledger) {
        int changed =
                jdbc.sql(
                                """
            UPDATE inventory_balance
            SET on_hand = :onHand, reserved = :reserved, version = version + 1
            WHERE sku_id = :sku AND warehouse_id = :warehouse
            """)
                        .param("onHand", ledger.onHand())
                        .param("reserved", ledger.reserved())
                        .param("sku", skuId(ledger.sku()))
                        .param("warehouse", ledger.warehouseId())
                        .update();
        if (changed != 1) {
            throw new InvalidInventoryMutationException("Inventory balance does not exist");
        }
        return ledger;
    }

    @Override
    @Transactional
    public boolean reserveAtomically(
            String requestKey, String sku, Long warehouseId, long quantity) {
        Long skuId = skuId(sku);
        var existing =
                jdbc.sql(
                                """
            SELECT sku_id, warehouse_id, quantity, status
            FROM inventory_reservation
            WHERE reservation_key = :key
            """)
                        .param("key", requestKey)
                        .query(new ColumnMapRowMapper())
                        .optional()
                        .orElse(null);
        if (existing != null) {
            boolean same =
                    ((Number) existing.get("sku_id")).longValue() == skuId
                            && ((Number) existing.get("warehouse_id")).longValue() == warehouseId
                            && ((Number) existing.get("quantity")).longValue() == quantity;
            if (!same) {
                throw new ReservationIdempotencyConflictException(requestKey);
            }
            return false;
        }

        int changed =
                jdbc.sql(
                                """
            UPDATE inventory_balance
            SET reserved = reserved + :quantity, version = version + 1
            WHERE sku_id = :sku
              AND warehouse_id = :warehouse
              AND on_hand - reserved >= :quantity
            """)
                        .param("quantity", quantity)
                        .param("sku", skuId)
                        .param("warehouse", warehouseId)
                        .update();
        if (changed != 1) {
            long available =
                    findBalance(skuId, warehouseId).map(InventoryBalance::available).orElse(0L);
            throw new com.dnnthanh.marketplace.be.inventory.api.domain.exception
                    .InsufficientStockException(sku, quantity, available);
        }

        LocalDateTime now = LocalDateTime.now();
        jdbc.sql(
                        """
            INSERT INTO inventory_reservation(
                reservation_key, sku_id, warehouse_id, quantity, status, expires_at,
                created_at, updated_at)
            VALUES(:key, :sku, :warehouse, :quantity, :status, :expiresAt, :now, :now)
            """)
                .param("key", requestKey)
                .param("sku", skuId)
                .param("warehouse", warehouseId)
                .param("quantity", quantity)
                .param("status", ReservationStatus.RESERVED.name())
                .param("expiresAt", now.plusMinutes(15))
                .param("now", now)
                .update();
        return true;
    }

    @Override
    @Transactional
    public void confirmReservation(String requestKey) {
        var reservation = lockReservation(requestKey);
        if (reservation == null
                || ReservationStatus.valueOf(String.valueOf(reservation.get("status")))
                        == ReservationStatus.CONFIRMED) {
            return;
        }
        if (ReservationStatus.valueOf(String.valueOf(reservation.get("status")))
                != ReservationStatus.RESERVED) {
            return;
        }
        long quantity = ((Number) reservation.get("quantity")).longValue();
        int changed =
                jdbc.sql(
                                """
            UPDATE inventory_balance
            SET on_hand = on_hand - :quantity,
                reserved = reserved - :quantity,
                version = version + 1
            WHERE sku_id = :sku AND warehouse_id = :warehouse
              AND reserved >= :quantity AND on_hand >= :quantity
            """)
                        .param("quantity", quantity)
                        .param("sku", reservation.get("sku_id"))
                        .param("warehouse", reservation.get("warehouse_id"))
                        .update();
        if (changed != 1) {
            throw new InvalidInventoryMutationException(
                    "Reservation confirmation violates inventory balance");
        }
        updateReservationStatus(requestKey, ReservationStatus.CONFIRMED);
        appendLedger(reservation, -quantity, "RESERVATION_CONFIRMED", requestKey);
    }

    @Override
    @Transactional
    public void releaseReservation(String requestKey, String reason) {
        var reservation = lockReservation(requestKey);
        if (reservation == null
                || ReservationStatus.valueOf(String.valueOf(reservation.get("status")))
                        != ReservationStatus.RESERVED) {
            return;
        }
        long quantity = ((Number) reservation.get("quantity")).longValue();
        int changed =
                jdbc.sql(
                                """
            UPDATE inventory_balance
            SET reserved = reserved - :quantity, version = version + 1
            WHERE sku_id = :sku AND warehouse_id = :warehouse AND reserved >= :quantity
            """)
                        .param("quantity", quantity)
                        .param("sku", reservation.get("sku_id"))
                        .param("warehouse", reservation.get("warehouse_id"))
                        .update();
        if (changed != 1) {
            throw new InvalidInventoryMutationException(
                    "Reservation release violates inventory balance");
        }
        updateReservationStatus(requestKey, ReservationStatus.RELEASED);
        appendLedger(
                reservation,
                0,
                reason == null || reason.isBlank() ? "RESERVATION_RELEASED" : reason,
                requestKey);
    }

    private java.util.Map<String, Object> lockReservation(String requestKey) {
        return jdbc.sql(
                        """
            SELECT id, sku_id, warehouse_id, quantity, status
            FROM inventory_reservation
            WHERE reservation_key = :key
            FOR UPDATE
            """)
                .param("key", requestKey)
                .query(new ColumnMapRowMapper())
                .optional()
                .orElse(null);
    }

    private void updateReservationStatus(String requestKey, ReservationStatus status) {
        jdbc.sql(
                        """
            UPDATE inventory_reservation
            SET status = :status, updated_at = :now
            WHERE reservation_key = :key
            """)
                .param("status", status.name())
                .param("now", LocalDateTime.now())
                .param("key", requestKey)
                .update();
    }

    @Override
    @Transactional
    public boolean adjust(
            String referenceKey, Long skuId, Long warehouseId, long delta, String reason) {
        Long duplicate =
                jdbc.sql(
                                """
            SELECT COUNT(*) FROM inventory_ledger
            WHERE reference_key=:referenceKey AND reason=:reason
            """)
                        .param("referenceKey", referenceKey)
                        .param("reason", "ADJUSTMENT_" + reason)
                        .query(Long.class)
                        .single();
        if (duplicate > 0) return false;

        var balance =
                jdbc.sql(
                                """
            SELECT on_hand,reserved FROM inventory_balance
            WHERE sku_id=:sku AND warehouse_id=:warehouse FOR UPDATE
            """)
                        .param("sku", skuId)
                        .param("warehouse", warehouseId)
                        .query()
                        .singleRow();
        long onHand = ((Number) balance.get("on_hand")).longValue();
        long reserved = ((Number) balance.get("reserved")).longValue();
        if (onHand + delta < reserved || onHand + delta < 0) {
            throw new InvalidInventoryMutationException(
                    "Adjustment would violate reserved/on-hand invariant");
        }
        jdbc.sql(
                        """
            UPDATE inventory_balance SET on_hand=on_hand+:delta,version=version+1
            WHERE sku_id=:sku AND warehouse_id=:warehouse
            """)
                .param("delta", delta)
                .param("sku", skuId)
                .param("warehouse", warehouseId)
                .update();
        appendLedger(skuId, warehouseId, delta, "ADJUSTMENT_" + reason, referenceKey);
        return true;
    }

    @Override
    @Transactional
    public boolean transfer(
            String transferKey,
            Long skuId,
            Long fromWarehouseId,
            Long toWarehouseId,
            long quantity) {
        Long duplicate =
                jdbc.sql(
                                """
            SELECT COUNT(*) FROM inventory_ledger
            WHERE reference_key=:key AND reason='TRANSFER_OUT'
            """)
                        .param("key", transferKey)
                        .query(Long.class)
                        .single();
        if (duplicate > 0) return false;

        long first = Math.min(fromWarehouseId, toWarehouseId);
        long second = Math.max(fromWarehouseId, toWarehouseId);
        jdbc.sql(
                        """
            SELECT warehouse_id FROM inventory_balance
            WHERE sku_id=:sku AND warehouse_id IN (:first,:second)
            ORDER BY warehouse_id FOR UPDATE
            """)
                .param("sku", skuId)
                .param("first", first)
                .param("second", second)
                .query(Long.class)
                .list();

        int removed =
                jdbc.sql(
                                """
            UPDATE inventory_balance
            SET on_hand=on_hand-:quantity,version=version+1
            WHERE sku_id=:sku AND warehouse_id=:warehouse
              AND (on_hand-reserved)>=:quantity
            """)
                        .param("quantity", quantity)
                        .param("sku", skuId)
                        .param("warehouse", fromWarehouseId)
                        .update();
        if (removed != 1) {
            throw new com.dnnthanh.marketplace.be.inventory.api.domain.exception
                    .InsufficientStockException(
                    String.valueOf(skuId), fromWarehouseId, quantity, 0);
        }
        int added =
                jdbc.sql(
                                """
            UPDATE inventory_balance
            SET on_hand=on_hand+:quantity,version=version+1
            WHERE sku_id=:sku AND warehouse_id=:warehouse
            """)
                        .param("quantity", quantity)
                        .param("sku", skuId)
                        .param("warehouse", toWarehouseId)
                        .update();
        if (added != 1) {
            throw new InvalidInventoryMutationException(
                    "Destination warehouse inventory balance not found");
        }
        appendLedger(skuId, fromWarehouseId, -quantity, "TRANSFER_OUT", transferKey);
        appendLedger(skuId, toWarehouseId, quantity, "TRANSFER_IN", transferKey);
        return true;
    }

    @Override
    public List<ReconciliationRow> reconcile(Long skuId, Long warehouseId, int limit) {
        return jdbc.sql(
                        """
            WITH active_reservation AS (
              SELECT sku_id,warehouse_id,SUM(quantity) quantity
              FROM inventory_reservation
              WHERE status='RESERVED' AND expires_at>now()
              GROUP BY sku_id,warehouse_id
            ), ledger AS (
              SELECT sku_id,warehouse_id,SUM(delta) net_delta
              FROM inventory_ledger
              GROUP BY sku_id,warehouse_id
            )
            SELECT b.sku_id,b.warehouse_id,b.on_hand,b.reserved,
                   COALESCE(r.quantity,0) active_reserved,
                   b.reserved-COALESCE(r.quantity,0) reserved_drift,
                   COALESCE(l.net_delta,0) ledger_net_delta
            FROM inventory_balance b
            LEFT JOIN active_reservation r USING(sku_id,warehouse_id)
            LEFT JOIN ledger l USING(sku_id,warehouse_id)
            WHERE (:sku IS NULL OR b.sku_id=:sku)
              AND (:warehouse IS NULL OR b.warehouse_id=:warehouse)
            ORDER BY ABS(b.reserved-COALESCE(r.quantity,0)) DESC,b.sku_id,b.warehouse_id
            LIMIT :limit
            """)
                .param("sku", skuId)
                .param("warehouse", warehouseId)
                .param("limit", limit)
                .query(
                        (rs, row) ->
                                new ReconciliationRow(
                                        rs.getLong("sku_id"),
                                        rs.getLong("warehouse_id"),
                                        rs.getLong("on_hand"),
                                        rs.getLong("reserved"),
                                        rs.getLong("active_reserved"),
                                        rs.getLong("reserved_drift"),
                                        rs.getLong("ledger_net_delta")))
                .list();
    }

    private void appendLedger(
            Long skuId, Long warehouseId, long delta, String reason, String referenceKey) {
        jdbc.sql(
                        """
            INSERT INTO inventory_ledger(sku_id,warehouse_id,delta,reason,reference_key,created_at)
            VALUES(:sku,:warehouse,:delta,:reason,:referenceKey,:createdAt)
            """)
                .param("sku", skuId)
                .param("warehouse", warehouseId)
                .param("delta", delta)
                .param("reason", reason)
                .param("referenceKey", referenceKey)
                .param("createdAt", LocalDateTime.now())
                .update();
    }

    private void appendLedger(
            java.util.Map<String, Object> reservation,
            long delta,
            String reason,
            String referenceKey) {
        jdbc.sql(
                        """
            INSERT INTO inventory_ledger(
                sku_id, warehouse_id, delta, reason, reference_key, created_at)
            VALUES(:sku, :warehouse, :delta, :reason, :referenceKey, :createdAt)
            """)
                .param("sku", reservation.get("sku_id"))
                .param("warehouse", reservation.get("warehouse_id"))
                .param("delta", delta)
                .param("reason", reason)
                .param("referenceKey", referenceKey)
                .param("createdAt", LocalDateTime.now())
                .update();
    }

    private static Long skuId(String sku) {
        try {
            return Long.valueOf(sku);
        } catch (NumberFormatException invalidSku) {
            throw new InvalidInventoryMutationException(
                    "Inventory SKU must be a numeric identifier");
        }
    }
}
