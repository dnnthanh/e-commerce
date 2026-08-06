package com.dnnthanh.marketplace.be.checkout.job.scheduler;

import com.dnnthanh.marketplace.be.checkout.job.adapter.out.internal.checkout.rest.CheckoutRecoveryRestAdapter;
import com.dnnthanh.marketplace.be.platform.stereotype.Adapter;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Detects durable Checkout Sagas ready for recovery and invokes the authenticated recovery
 * boundary.
 */
@Adapter
@RequiredArgsConstructor
@Slf4j
public class CheckoutSagaRecoveryJob {

    private final JdbcClient jdbc;

    private final CheckoutRecoveryRestAdapter recoveryClient;

    /** Processes a bounded recovery batch without creating ad-hoc Kafka JSON commands. */
    @Scheduled(fixedDelayString = "${checkout.recovery.poll-ms:10000}")
    public void recover() {
        jdbc.sql(
                        """
                SELECT checkout_key
                FROM checkout_saga
                WHERE status IN ('FAILED_RETRYABLE','PAYMENT_UNKNOWN')
                  AND next_retry_at <= :now
                ORDER BY next_retry_at
                LIMIT 100
                """)
                .param("now", LocalDateTime.now())
                .query(String.class)
                .list()
                .forEach(this::recoverOne);
    }

    private void recoverOne(String checkoutKey) {
        try {
            recoveryClient.recover(checkoutKey);
        } catch (RuntimeException failure) {
            log.warn(
                    "checkout_recovery_failed checkoutKey={} failure={}",
                    checkoutKey,
                    failure.toString());
        }
    }
}
