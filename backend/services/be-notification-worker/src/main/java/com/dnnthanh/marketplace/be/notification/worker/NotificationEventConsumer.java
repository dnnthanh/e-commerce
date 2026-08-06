package com.dnnthanh.marketplace.be.notification.worker;

import com.dnnthanh.marketplace.be.notification.worker.exception.UnsupportedNotificationEventException;
import com.dnnthanh.marketplace.be.platform.event.DomainEvent;
import com.dnnthanh.marketplace.be.platform.kafka.BaseDomainEventConsumer;
import com.dnnthanh.marketplace.be.platform.kafka.DomainEventProducer;
import com.dnnthanh.marketplace.be.platform.stereotype.Adapter;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** Converts typed business events into durable Mongo notifications with Inbox idempotency. */
@Adapter
@RequiredArgsConstructor
public class NotificationEventConsumer extends BaseDomainEventConsumer {

    private final MongoTemplate mongoTemplate;

    private final DomainEventProducer eventProducer;

    /**
     * Converts a comment reply into one durable direct-user notification.
     *
     * @param event typed comment event
     */
    @KafkaListener(topics = "marketplace.comment.events", groupId = "notification-comment-v3")
    @Transactional
    public void comment(DomainEvent event) {
        if (!accepts(event, "COMMENT_REPLIED")) {
            return;
        }
        direct(
                "comment",
                event,
                requiredString(event, "recipientUserId"),
                "COMMENT_REPLIED",
                "Có phản hồi mới",
                "Bình luận của bạn vừa có phản hồi.");
    }

    /**
     * Converts final payment outcomes into durable customer notifications.
     *
     * @param event typed payment event
     */
    @KafkaListener(topics = "marketplace.payment.events", groupId = "notification-payment-v3")
    @Transactional
    public void payment(DomainEvent event) {
        if (!accepts(event, "PAYMENT_SUCCEEDED", "PAYMENT_FAILED", "PAYMENT_REFUNDED")) {
            return;
        }
        Object userId = payload(event).get("userId");
        if (userId == null) {
            return;
        }
        String title =
                switch (event.eventType()) {
                    case "PAYMENT_SUCCEEDED" -> "Thanh toán thành công";
                    case "PAYMENT_FAILED" -> "Thanh toán thất bại";
                    case "PAYMENT_REFUNDED" -> "Hoàn tiền cập nhật";
                    default -> throw new UnsupportedNotificationEventException(event.eventType());
                };
        direct(
                "payment",
                event,
                String.valueOf(userId),
                event.eventType(),
                title,
                title + " cho đơn hàng của bạn.");
    }

    @KafkaListener(topics = "marketplace.seller.events", groupId = "notification-seller-v3")
    @Transactional
    public void seller(DomainEvent event) {
        if (!accepts(event, "SELLER_MATERIAL_INFO_CHANGED") || !claim("seller", event.eventId())) {
            return;
        }
        long sellerId = requiredLong(event, "sellerId");
        mongoTemplate.insert(
                new Document("_id", event.eventId())
                        .append("sellerId", sellerId)
                        .append("eventType", event.eventType())
                        .append("payload", new Document(payload(event)))
                        .append("cursor", null)
                        .append("status", "PENDING")
                        .append("createdAt", LocalDateTime.now()),
                "notification_fanout_job");
    }

    private void direct(
            String source,
            DomainEvent event,
            String userId,
            String type,
            String title,
            String message) {
        if (!claim(source, event.eventId())) {
            return;
        }
        String notificationId = UUID.randomUUID().toString();
        mongoTemplate.insert(
                new Document("_id", notificationId)
                        .append("userId", userId)
                        .append("type", type)
                        .append("title", title)
                        .append("message", message)
                        .append("payload", new Document(payload(event)))
                        .append("readAt", null)
                        .append("createdAt", LocalDateTime.now()),
                "notification");
        publishAfterCommit(userId, notificationId, type);
    }

    private boolean claim(String source, String eventId) {
        String inboxId = source + ":" + eventId;
        if (mongoTemplate.exists(
                Query.query(Criteria.where("_id").is(inboxId)), "notification_inbox")) {
            return false;
        }
        mongoTemplate.insert(
                new Document("_id", inboxId)
                        .append("source", source)
                        .append("eventId", eventId)
                        .append("processedAt", LocalDateTime.now()),
                "notification_inbox");
        return true;
    }

    private void publishAfterCommit(String userId, String notificationId, String type) {
        Runnable publish =
                () ->
                        eventProducer.publish(
                                "marketplace.notification.realtime",
                                userId,
                                new DomainEvent(
                                        UUID.randomUUID().toString(),
                                        "NOTIFICATION_READY",
                                        notificationId,
                                        LocalDateTime.now(),
                                        null,
                                        userId,
                                        null,
                                        Map.of(
                                                "notificationId",
                                                notificationId,
                                                "userId",
                                                userId,
                                                "type",
                                                type)));
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {

                        /** Publishes only after the durable Mongo transaction commits. */
                        @Override
                        public void afterCommit() {
                            publish.run();
                        }
                    });
            return;
        }
        publish.run();
    }
}
