package com.dnnthanh.marketplace.be.review.worker;

import com.dnnthanh.marketplace.be.platform.stereotype.Persistence;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;

@Persistence
@RequiredArgsConstructor
public class VerifiedPurchaseMaterializer {

    private final JdbcClient jdbc;

    @Transactional
    public void materialize(String eventId, String userId, List<VerifiedPurchaseLine> lines) {
        if (!claim(eventId)) {
            return;
        }

        LocalDateTime deliveredAt = LocalDateTime.now();
        for (VerifiedPurchaseLine line : lines) {
            jdbc.sql(
                            """
                            INSERT IGNORE INTO verified_purchase(
                                user_id, order_line_id, product_id, sku_id, delivered_at)
                            VALUES(:userId, :orderLineId, :productId, :skuId, :deliveredAt)
                            """)
                    .param("userId", userId)
                    .param("orderLineId", line.orderLineId())
                    .param("productId", line.productId())
                    .param("skuId", line.skuId())
                    .param("deliveredAt", deliveredAt)
                    .update();
        }
    }

    private boolean claim(String eventId) {
        try {
            jdbc.sql(
                            """
                            INSERT INTO inbox_event(consumer_name, event_id, processed_at)
                            VALUES('review-fulfillment-v3', :eventId, :processedAt)
                            """)
                    .param("eventId", eventId)
                    .param("processedAt", LocalDateTime.now())
                    .update();
            return true;
        } catch (DuplicateKeyException duplicate) {
            return false;
        }
    }
}
