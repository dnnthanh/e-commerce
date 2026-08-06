package com.dnnthanh.marketplace.be.order.outbox.publisher;

import com.dnnthanh.marketplace.be.platform.event.DomainEvent;
import com.dnnthanh.marketplace.be.platform.event.OutboxEventFactory;
import com.dnnthanh.marketplace.be.platform.kafka.DomainEventProducer;
import com.dnnthanh.marketplace.be.platform.outbox.OutboxRetryPolicy;
import com.dnnthanh.marketplace.be.platform.stereotype.Adapter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Scheduled;

/** SQL Server outbox publisher with READPAST/UPDLOCK leasing for horizontal workers. */
@Adapter
@RequiredArgsConstructor
public class OrderOutboxPublisher {
    private static final int BATCH_SIZE = 100;
    private static final int LEASE_SECONDS = 30;
    private static final int MAX_ERROR_LENGTH = 2000;
    private static final OutboxRetryPolicy RETRY_POLICY = OutboxRetryPolicy.defaults();

    private final JdbcClient jdbc;
    private final DomainEventProducer kafka;
    private final OutboxEventFactory eventFactory;

    /** Claims one non-overlapping batch, publishes it, and settles each lease asynchronously. */
    @Scheduled(fixedDelayString = "${outbox.poll-ms:1000}")
    public void publishBatch() {
        for (Map<String, Object> row : claimBatch()) {
            publish(row);
        }
    }

    private List<Map<String, Object>> claimBatch() {
        return jdbc.sql(
                        """
                        ;WITH candidates AS (
                            SELECT TOP (:batchSize) *
                            FROM outbox_event WITH (UPDLOCK, READPAST, ROWLOCK)
                            WHERE (status='PENDING'
                                      AND (next_attempt_at IS NULL OR next_attempt_at <= SYSDATETIME()))
                               OR (status='PUBLISHING' AND locked_until <= SYSDATETIME())
                            ORDER BY id
                        )
                        UPDATE candidates
                        SET status='PUBLISHING',
                            locked_until=DATEADD(second,:leaseSeconds,SYSDATETIME()),
                            attempt_count=attempt_count + 1,
                            last_error=NULL
                        OUTPUT inserted.id,inserted.event_id,inserted.aggregate_id,inserted.event_type,
                               inserted.payload_json,inserted.attempt_count
                        """)
                .param("batchSize", BATCH_SIZE)
                .param("leaseSeconds", LEASE_SECONDS)
                .query()
                .listOfRows();
    }

    private void publish(Map<String, Object> row) {
        long id = number(row, "id");
        int attemptCount = Math.toIntExact(number(row, "attempt_count"));
        String aggregateId = String.valueOf(row.get("aggregate_id"));
        DomainEvent event =
                eventFactory.fromOutbox(
                        String.valueOf(row.get("event_id")),
                        String.valueOf(row.get("event_type")),
                        aggregateId,
                        String.valueOf(row.get("payload_json")));
        kafka.publish("marketplace.order.events", aggregateId, event)
                .whenComplete(
                        (result, error) -> {
                            if (error == null) {
                                markProcessed(id);
                            } else {
                                markFailedAttempt(id, attemptCount, error);
                            }
                        });
    }

    private void markProcessed(long id) {
        jdbc.sql(
                        """
                        UPDATE outbox_event
                        SET status='PROCESSED', processed_at=SYSDATETIME(), locked_until=NULL,
                            next_attempt_at=NULL, last_error=NULL
                        WHERE id=:id AND status='PUBLISHING'
                        """)
                .param("id", id)
                .update();
    }

    private void markFailedAttempt(long id, int attemptCount, Throwable error) {
        boolean terminal = RETRY_POLICY.terminal(attemptCount);
        LocalDateTime nextAttempt =
                terminal ? null : LocalDateTime.now().plus(RETRY_POLICY.nextDelay(attemptCount));
        jdbc.sql(
                        """
                        UPDATE outbox_event
                        SET status=:status, locked_until=NULL, next_attempt_at=:nextAttempt,
                            last_error=:lastError
                        WHERE id=:id AND status='PUBLISHING'
                        """)
                .param("status", terminal ? "FAILED" : "PENDING")
                .param("nextAttempt", nextAttempt)
                .param("lastError", abbreviate(error))
                .param("id", id)
                .update();
    }

    private static long number(Map<String, Object> row, String key) {
        return ((Number) row.get(key)).longValue();
    }

    private static String abbreviate(Throwable error) {
        String message = error.toString();
        return message.length() <= MAX_ERROR_LENGTH
                ? message
                : message.substring(0, MAX_ERROR_LENGTH);
    }
}
