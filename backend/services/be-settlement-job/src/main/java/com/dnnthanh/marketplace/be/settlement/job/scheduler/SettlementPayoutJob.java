package com.dnnthanh.marketplace.be.settlement.job.scheduler;

import com.dnnthanh.marketplace.be.platform.stereotype.Adapter;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

/**
 * Converts approved settlements into durable payout requests without publishing Kafka inside the
 * scheduler transaction.
 */
@Adapter
public class SettlementPayoutJob {

    private final JdbcClient jdbc;

    public SettlementPayoutJob(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** Claims a bounded batch and writes source-owned Outbox rows. */
    @Scheduled(cron = "${settlement.payout.cron:0 */5 * * * *}")
    @Transactional
    public void enqueue() {
        jdbc.sql(
                        """
                SELECT settlement_no
                FROM settlement
                WHERE status='APPROVED'
                ORDER BY id
                FETCH FIRST 100 ROWS ONLY
                """)
                .query(String.class)
                .list()
                .forEach(this::enqueueOne);
    }

    private void enqueueOne(String settlementNo) {
        int changed =
                jdbc.sql(
                                """
                UPDATE settlement
                SET status='PAYOUT_PENDING',updated_at=SYSTIMESTAMP
                WHERE settlement_no=:no AND status='APPROVED'
                """)
                        .param("no", settlementNo)
                        .update();
        if (changed == 0) return;
        String payload = "{\"settlementNo\":\"" + settlementNo + "\"}";
        jdbc.sql(
                        """
                INSERT INTO outbox_event(event_id,aggregate_id,event_type,payload_json,status,created_at)
                VALUES(:event,:aggregate,'SETTLEMENT_PAYOUT_REQUESTED',:payload,'PENDING',SYSTIMESTAMP)
                """)
                .param("event", UUID.randomUUID().toString())
                .param("aggregate", settlementNo)
                .param("payload", payload)
                .update();
    }
}
