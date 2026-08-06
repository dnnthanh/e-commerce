package com.dnnthanh.marketplace.be.promotion.api.adapter.out.persistence;

import com.dnnthanh.marketplace.be.platform.stereotype.Persistence;
import com.dnnthanh.marketplace.be.promotion.api.application.port.out.PromotionUsagePort;
import com.dnnthanh.marketplace.be.promotion.api.domain.enumtype.PromotionReservationStatus;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;

/**
 * Atomic PostgreSQL usage-reservation adapter.
 *
 * <p>JDBC is intentional because the reservation path needs row locks and conditional counters to
 * make global/per-customer limits race-safe under concurrent checkout requests.
 */
@Persistence
@RequiredArgsConstructor
public class JdbcPromotionUsageAdapter implements PromotionUsagePort {
    private final JdbcClient jdbc;

    @Override
    @Transactional
    public boolean reserve(
            String reservationKey,
            String promotionId,
            String customerId,
            long globalLimit,
            long customerLimit) {
        Map<String, Object> existing =
                jdbc.sql(
                                """
                        SELECT promotion_id, customer_id, status
                        FROM promotion_usage_reservation
                        WHERE reservation_key=:reservationKey
                        """)
                        .param("reservationKey", reservationKey)
                        .query(new ColumnMapRowMapper())
                        .optional()
                        .orElse(null);
        if (existing != null) {
            return promotionId.equals(String.valueOf(existing.get("promotion_id")))
                    && customerId.equals(String.valueOf(existing.get("customer_id")))
                    && PromotionReservationStatus.RELEASED
                            != PromotionReservationStatus.valueOf(
                                    String.valueOf(existing.get("status")));
        }

        jdbc.sql(
                        """
                        INSERT INTO promotion_usage_counter(promotion_id, reserved_count, confirmed_count)
                        VALUES(:promotionId, 0, 0)
                        ON CONFLICT (promotion_id) DO NOTHING
                        """)
                .param("promotionId", promotionId)
                .update();
        jdbc.sql(
                        """
                        INSERT INTO promotion_customer_usage(
                            promotion_id, customer_id, reserved_count, confirmed_count)
                        VALUES(:promotionId, :customerId, 0, 0)
                        ON CONFLICT (promotion_id, customer_id) DO NOTHING
                        """)
                .param("promotionId", promotionId)
                .param("customerId", customerId)
                .update();

        Map<String, Object> global =
                jdbc.sql(
                                """
                        SELECT reserved_count, confirmed_count
                        FROM promotion_usage_counter
                        WHERE promotion_id=:promotionId
                        FOR UPDATE
                        """)
                        .param("promotionId", promotionId)
                        .query()
                        .singleRow();
        Map<String, Object> customer =
                jdbc.sql(
                                """
                        SELECT reserved_count, confirmed_count
                        FROM promotion_customer_usage
                        WHERE promotion_id=:promotionId AND customer_id=:customerId
                        FOR UPDATE
                        """)
                        .param("promotionId", promotionId)
                        .param("customerId", customerId)
                        .query()
                        .singleRow();

        long globalUsed =
                number(global.get("reserved_count")) + number(global.get("confirmed_count"));
        long customerUsed =
                number(customer.get("reserved_count")) + number(customer.get("confirmed_count"));
        if ((globalLimit > 0 && globalUsed >= globalLimit)
                || (customerLimit > 0 && customerUsed >= customerLimit)) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        jdbc.sql(
                        """
                        INSERT INTO promotion_usage_reservation(
                            reservation_key, promotion_id, customer_id, status, created_at, updated_at)
                        VALUES(:reservationKey, :promotionId, :customerId, :status, :createdAt, :updatedAt)
                        """)
                .param("reservationKey", reservationKey)
                .param("promotionId", promotionId)
                .param("customerId", customerId)
                .param("status", PromotionReservationStatus.RESERVED.name())
                .param("createdAt", now)
                .param("updatedAt", now)
                .update();
        incrementReserved(promotionId, customerId, 1);
        return true;
    }

    @Override
    @Transactional
    public void confirm(String reservationKey) {
        Reservation reservation = lockReservation(reservationKey);
        if (reservation == null || reservation.status() == PromotionReservationStatus.CONFIRMED) {
            return;
        }
        if (reservation.status() == PromotionReservationStatus.RELEASED) {
            return;
        }
        jdbc.sql(
                        """
                        UPDATE promotion_usage_reservation
                        SET status=:status, updated_at=:updatedAt
                        WHERE reservation_key=:reservationKey AND status=:expected
                        """)
                .param("status", PromotionReservationStatus.CONFIRMED.name())
                .param("updatedAt", LocalDateTime.now())
                .param("reservationKey", reservationKey)
                .param("expected", PromotionReservationStatus.RESERVED.name())
                .update();
        incrementReserved(reservation.promotionId(), reservation.customerId(), -1);
        incrementConfirmed(reservation.promotionId(), reservation.customerId(), 1);
    }

    @Override
    @Transactional
    public void release(String reservationKey) {
        Reservation reservation = lockReservation(reservationKey);
        if (reservation == null || reservation.status() != PromotionReservationStatus.RESERVED) {
            return;
        }
        jdbc.sql(
                        """
                        UPDATE promotion_usage_reservation
                        SET status=:status, updated_at=:updatedAt
                        WHERE reservation_key=:reservationKey AND status=:expected
                        """)
                .param("status", PromotionReservationStatus.RELEASED.name())
                .param("updatedAt", LocalDateTime.now())
                .param("reservationKey", reservationKey)
                .param("expected", PromotionReservationStatus.RESERVED.name())
                .update();
        incrementReserved(reservation.promotionId(), reservation.customerId(), -1);
    }

    private Reservation lockReservation(String reservationKey) {
        return jdbc.sql(
                        """
                        SELECT promotion_id, customer_id, status
                        FROM promotion_usage_reservation
                        WHERE reservation_key=:reservationKey
                        FOR UPDATE
                        """)
                .param("reservationKey", reservationKey)
                .query(
                        (rs, rowNum) ->
                                new Reservation(
                                        rs.getString("promotion_id"),
                                        rs.getString("customer_id"),
                                        PromotionReservationStatus.valueOf(rs.getString("status"))))
                .optional()
                .orElse(null);
    }

    private void incrementReserved(String promotionId, String customerId, int delta) {
        jdbc.sql(
                        """
                        UPDATE promotion_usage_counter
                        SET reserved_count=reserved_count+:delta
                        WHERE promotion_id=:promotionId
                        """)
                .param("delta", delta)
                .param("promotionId", promotionId)
                .update();
        jdbc.sql(
                        """
                        UPDATE promotion_customer_usage
                        SET reserved_count=reserved_count+:delta
                        WHERE promotion_id=:promotionId AND customer_id=:customerId
                        """)
                .param("delta", delta)
                .param("promotionId", promotionId)
                .param("customerId", customerId)
                .update();
    }

    private void incrementConfirmed(String promotionId, String customerId, int delta) {
        jdbc.sql(
                        """
                        UPDATE promotion_usage_counter
                        SET confirmed_count=confirmed_count+:delta
                        WHERE promotion_id=:promotionId
                        """)
                .param("delta", delta)
                .param("promotionId", promotionId)
                .update();
        jdbc.sql(
                        """
                        UPDATE promotion_customer_usage
                        SET confirmed_count=confirmed_count+:delta
                        WHERE promotion_id=:promotionId AND customer_id=:customerId
                        """)
                .param("delta", delta)
                .param("promotionId", promotionId)
                .param("customerId", customerId)
                .update();
    }

    private static long number(Object value) {
        return value == null ? 0L : ((Number) value).longValue();
    }

    private record Reservation(
            String promotionId, String customerId, PromotionReservationStatus status) {}
}
