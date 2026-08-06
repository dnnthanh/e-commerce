package com.dnnthanh.marketplace.be.inventory.worker;

import com.dnnthanh.marketplace.be.inventory.worker.exception.InvalidReturnRestockEventException;
import com.dnnthanh.marketplace.be.platform.event.DomainEvent;
import com.dnnthanh.marketplace.be.platform.kafka.BaseDomainEventConsumer;
import com.dnnthanh.marketplace.be.platform.stereotype.Adapter;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.transaction.annotation.Transactional;

/** Applies inspected returned stock to the receiving warehouse idempotently. */
@Adapter
@RequiredArgsConstructor
public class ReturnRestockConsumer extends BaseDomainEventConsumer {

    private final JdbcClient jdbc;

    /**
     * Claims the return event and updates balance plus ledger in one transaction.
     *
     * @param event typed return event
     */
    @Transactional
    @KafkaListener(topics = "marketplace.return.events", groupId = "inventory-return-restock-v1")
    public void onReturn(DomainEvent event) {
        if (!accepts(event, "RETURN_ITEM_RESTOCK_REQUESTED") || !claim(event.eventId())) {
            return;
        }
        Long skuId = longValue(event.payload().get("skuId"));
        Long warehouseId = longValue(event.payload().get("warehouseId"));
        long quantity = longValue(event.payload().get("quantity"));
        String returnKey = String.valueOf(event.payload().get("returnKey"));
        if (skuId == null || warehouseId == null || quantity <= 0) {
            throw new InvalidReturnRestockEventException(
                    "Invalid return restock payload for " + event.eventId());
        }

        int updated =
                jdbc.sql(
                                """
                UPDATE inventory_balance
                SET on_hand = on_hand + :quantity, version = version + 1
                WHERE sku_id=:skuId AND warehouse_id=:warehouseId
                """)
                        .param("quantity", quantity)
                        .param("skuId", skuId)
                        .param("warehouseId", warehouseId)
                        .update();
        if (updated == 0) {
            jdbc.sql(
                            """
                    INSERT INTO inventory_balance(sku_id, warehouse_id, on_hand, reserved, version)
                    VALUES(:skuId, :warehouseId, :quantity, 0, 0)
                    """)
                    .param("skuId", skuId)
                    .param("warehouseId", warehouseId)
                    .param("quantity", quantity)
                    .update();
        }
        jdbc.sql(
                        """
                INSERT INTO inventory_ledger(
                    sku_id, warehouse_id, delta, reason, reference_key, created_at)
                VALUES(:skuId, :warehouseId, :quantity, 'RETURN_RESTOCK', :returnKey, :createdAt)
                """)
                .param("skuId", skuId)
                .param("warehouseId", warehouseId)
                .param("quantity", quantity)
                .param("returnKey", returnKey)
                .param("createdAt", LocalDateTime.now())
                .update();
    }

    private boolean claim(String eventId) {
        try {
            jdbc.sql(
                            """
                    INSERT INTO inbox_event(consumer_name, event_id, processed_at)
                    VALUES('inventory-return-restock-v1', :eventId, :processedAt)
                    """)
                    .param("eventId", eventId)
                    .param("processedAt", LocalDateTime.now())
                    .update();
            return true;
        } catch (DuplicateKeyException duplicate) {
            return false;
        }
    }

    private Long longValue(Object value) {
        return value == null ? null : Long.valueOf(String.valueOf(value));
    }
}
