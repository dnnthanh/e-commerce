package com.dnnthanh.marketplace.be.order.worker;

import com.dnnthanh.marketplace.be.order.worker.exception.UnsupportedPaymentEventException;
import com.dnnthanh.marketplace.be.platform.event.DomainEvent;
import com.dnnthanh.marketplace.be.platform.kafka.BaseDomainEventConsumer;
import com.dnnthanh.marketplace.be.platform.stereotype.Adapter;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.transaction.annotation.Transactional;

/** Updates order state from typed payment events using transactional Inbox idempotency. */
@Adapter
@RequiredArgsConstructor
public class PaymentStatusConsumer extends BaseDomainEventConsumer {

    private final JdbcClient jdbc;

    /**
     * Handles supported payment-state transitions without manual JSON deserialization.
     *
     * @param event typed payment event
     */
    @KafkaListener(topics = "marketplace.payment.events", groupId = "be-order-worker-v2")
    @Transactional
    public void handle(DomainEvent event) {
        if (!accepts(event, "PAYMENT_SUCCEEDED", "PAYMENT_FAILED", "PAYMENT_UNKNOWN")) {
            return;
        }
        if (!claim(event.eventId())) {
            return;
        }
        String orderId = requiredString(event, "orderId");
        String orderStatus =
                switch (event.eventType()) {
                    case "PAYMENT_SUCCEEDED" -> "PAID";
                    case "PAYMENT_FAILED" -> "PAYMENT_FAILED";
                    case "PAYMENT_UNKNOWN" -> "PAYMENT_UNKNOWN";
                    default -> throw new UnsupportedPaymentEventException(event.eventType());
                };
        jdbc.sql(
                        "UPDATE marketplace_order SET status=:status,updated_at=SYSDATETIME() WHERE order_no=:order")
                .param("status", orderStatus)
                .param("order", orderId)
                .update();
    }

    private boolean claim(String eventId) {
        try {
            jdbc.sql(
                            "INSERT INTO inbox_event(consumer_name,event_id,processed_at) VALUES('order-payment-v2',:id,:at)")
                    .param("id", eventId)
                    .param("at", LocalDateTime.now())
                    .update();
            return true;
        } catch (DuplicateKeyException duplicate) {
            return false;
        }
    }
}
