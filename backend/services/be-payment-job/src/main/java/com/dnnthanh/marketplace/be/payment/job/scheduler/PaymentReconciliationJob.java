package com.dnnthanh.marketplace.be.payment.job.scheduler;

import com.dnnthanh.marketplace.be.payment.job.adapter.out.internal.payment.rest.PaymentReconciliationRestAdapter;
import com.dnnthanh.marketplace.be.platform.stereotype.Adapter;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Reconciles UNKNOWN payments through the authenticated internal Payment boundary rather than
 * recharging them.
 */
@Adapter
@RequiredArgsConstructor
@Slf4j
public class PaymentReconciliationJob {

    private final JdbcClient jdbc;

    private final PaymentReconciliationRestAdapter reconciliationClient;

    /** Processes a bounded batch of due UNKNOWN payments. */
    @Scheduled(fixedDelayString = "${payment.reconcile.poll-ms:15000}")
    public void reconcile() {
        jdbc.sql(
                        """
                SELECT p.payment_key
                FROM payment p
                JOIN payment_reconciliation_state r ON r.payment_id = p.id
                WHERE p.status = 'UNKNOWN'
                  AND (r.next_retry_at IS NULL OR r.next_retry_at <= :now)
                ORDER BY p.id
                LIMIT 100
                """)
                .param("now", LocalDateTime.now())
                .query(String.class)
                .list()
                .forEach(this::reconcileOne);
    }

    private void reconcileOne(String paymentKey) {
        try {
            reconciliationClient.reconcile(paymentKey);
        } catch (RuntimeException failure) {
            log.warn(
                    "payment_reconciliation_failed paymentKey={} failure={}",
                    paymentKey,
                    failure.toString());
        }
    }
}
