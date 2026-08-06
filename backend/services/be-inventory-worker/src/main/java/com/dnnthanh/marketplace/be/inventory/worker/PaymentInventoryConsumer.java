package com.dnnthanh.marketplace.be.inventory.worker;

import com.dnnthanh.marketplace.be.platform.event.DomainEvent;
import com.dnnthanh.marketplace.be.platform.kafka.BaseDomainEventConsumer;
import com.dnnthanh.marketplace.be.platform.stereotype.Adapter;
import java.time.LocalDateTime;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.transaction.annotation.Transactional;

/** Applies payment outcomes to inventory reservations with Inbox idempotency. */
@Adapter
public class PaymentInventoryConsumer extends BaseDomainEventConsumer {

    private final JdbcClient jdbc;

    public PaymentInventoryConsumer(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Confirms reservations after payment success and releases them after definitive payment
     * failure. Inbox claim and business mutation share the same transaction so a process crash
     * cannot lose an event.
     */
    @Transactional
    @KafkaListener(topics = "marketplace.payment.events", groupId = "inventory-payment-v1")
    public void onPayment(DomainEvent event) {
        if (!accepts(event, "PAYMENT_SUCCEEDED", "PAYMENT_FAILED")) return;
        if (!claim(event.eventId())) return;
        String orderId = string(event.payload().get("orderId"));
        if (orderId == null) return;
        if (event.eventType().equals("PAYMENT_SUCCEEDED")) {
            jdbc.sql(
                            """
                    UPDATE inventory_reservation SET status='CONFIRMED',updated_at=:now
                    WHERE order_id=:orderId AND status='RESERVED'
                    """)
                    .param("now", LocalDateTime.now())
                    .param("orderId", orderId)
                    .update();
        } else {
            var reservations =
                    jdbc.sql(
                                    """
                    SELECT id,sku_id,warehouse_id,quantity FROM inventory_reservation
                    WHERE order_id=:orderId AND status='RESERVED' FOR UPDATE
                    """)
                            .param("orderId", orderId)
                            .query()
                            .listOfRows();
            for (var reservation : reservations) {
                jdbc.sql(
                                """
                        UPDATE inventory_balance SET reserved=reserved-:quantity,version=version+1
                        WHERE sku_id=:skuId AND warehouse_id=:warehouseId AND reserved>=:quantity
                        """)
                        .param("quantity", reservation.get("quantity"))
                        .param("skuId", reservation.get("sku_id"))
                        .param("warehouseId", reservation.get("warehouse_id"))
                        .update();
                jdbc.sql(
                                "UPDATE inventory_reservation SET status='RELEASED',updated_at=:now WHERE id=:id")
                        .param("now", LocalDateTime.now())
                        .param("id", reservation.get("id"))
                        .update();
            }
        }
    }

    private boolean claim(String eventId) {
        try {
            jdbc.sql(
                            "INSERT INTO inbox_event(consumer_name,event_id,processed_at) VALUES('inventory-payment-v1',:eventId,:at)")
                    .param("eventId", eventId)
                    .param("at", LocalDateTime.now())
                    .update();
            return true;
        } catch (DuplicateKeyException duplicate) {
            return false;
        }
    }

    private String string(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
