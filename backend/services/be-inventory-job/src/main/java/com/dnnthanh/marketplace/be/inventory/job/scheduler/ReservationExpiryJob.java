package com.dnnthanh.marketplace.be.inventory.job.scheduler;

import com.dnnthanh.marketplace.be.platform.stereotype.Adapter;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

/** Releases expired reservations without leaving inventory reserved quantity inflated. */
@Adapter
@RequiredArgsConstructor
public class ReservationExpiryJob {

    private static final int BATCH_SIZE = 200;

    private static final String CLAIM_EXPIRED_SQL =
            """
      SELECT id,sku_id,warehouse_id,quantity
      FROM inventory_reservation
      WHERE status='RESERVED' AND expires_at<:now
      ORDER BY id
      LIMIT %d
      FOR UPDATE SKIP LOCKED
      """
                    .formatted(BATCH_SIZE);

    private final JdbcClient jdbc;

    /** Processes a bounded batch so the job does not monopolize the Inventory database. */
    @Scheduled(fixedDelayString = "${inventory.expiry.poll-ms:5000}")
    @Transactional
    public void expireBatch() {
        var rows =
                jdbc.sql(CLAIM_EXPIRED_SQL).param("now", LocalDateTime.now()).query().listOfRows();

        for (Map<String, Object> row : rows) {
            releaseReservedQuantity(row);
            markExpired(row.get("id"));
        }
    }

    private void releaseReservedQuantity(Map<String, Object> row) {
        jdbc.sql(
                        """
            UPDATE inventory_balance
            SET reserved=reserved-:quantity, version=version+1
            WHERE sku_id=:sku AND warehouse_id=:warehouse AND reserved>=:quantity
            """)
                .param("quantity", row.get("quantity"))
                .param("sku", row.get("sku_id"))
                .param("warehouse", row.get("warehouse_id"))
                .update();
    }

    private void markExpired(Object reservationId) {
        jdbc.sql("UPDATE inventory_reservation SET status='EXPIRED',updated_at=:at WHERE id=:id")
                .param("at", LocalDateTime.now())
                .param("id", reservationId)
                .update();
    }
}
