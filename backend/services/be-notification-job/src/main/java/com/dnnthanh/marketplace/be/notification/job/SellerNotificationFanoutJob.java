package com.dnnthanh.marketplace.be.notification.job;

import com.dnnthanh.marketplace.be.platform.event.DomainEvent;
import com.dnnthanh.marketplace.be.platform.kafka.DomainEventProducer;
import com.dnnthanh.marketplace.be.platform.stereotype.Adapter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;

/** Resumable seller-change fanout with checkpointed batches and duplicate-safe notifications. */
@Adapter
@RequiredArgsConstructor
public class SellerNotificationFanoutJob {

    private static final int BATCH_SIZE = 500;
    private static final String JOB_COLLECTION = "notification_fanout_job";
    private static final String FOLLOW_COLLECTION = "seller_follow";
    private static final String PREFERENCE_COLLECTION = "notification_preference";
    private static final String NOTIFICATION_COLLECTION = "notification";
    private static final String NOTIFICATION_TYPE = "SELLER_MATERIAL_INFO_CHANGED";
    private static final String REALTIME_TOPIC = "marketplace.notification.realtime";

    private final MongoTemplate mongo;
    private final DomainEventProducer kafka;

    /** Processes one durable fanout job and at most one follower page per scheduler run. */
    @Scheduled(fixedDelayString = "${notification.fanout.poll-ms:1000}")
    public void run() {
        Document job = findNextJob();
        if (job == null) {
            return;
        }

        List<Document> followers = loadFollowerBatch(job);
        if (followers.isEmpty()) {
            complete(job);
            return;
        }

        for (Document follower : followers) {
            deliverIfEligible(job, follower);
        }

        checkpoint(job, followers.getLast().getString("userId"));
    }

    private Document findNextJob() {
        Query query =
                Query.query(Criteria.where("status").in("PENDING", "RUNNING"))
                        .with(Sort.by("createdAt"));
        return mongo.findOne(query, Document.class, JOB_COLLECTION);
    }

    private List<Document> loadFollowerBatch(Document job) {
        Long sellerId = job.getLong("sellerId");
        String cursor = job.getString("cursor");

        Criteria criteria = Criteria.where("sellerId").is(sellerId).and("active").is(true);
        if (cursor != null) {
            criteria = criteria.and("userId").gt(cursor);
        }

        return mongo.find(
                Query.query(criteria).with(Sort.by("userId")).limit(BATCH_SIZE),
                Document.class,
                FOLLOW_COLLECTION);
    }

    private void deliverIfEligible(Document job, Document follower) {
        String userId = follower.getString("userId");
        if (!sellerUpdatesEnabled(userId)) {
            return;
        }

        String notificationId = job.getString("_id") + ":" + userId;
        if (mongo.exists(
                Query.query(Criteria.where("_id").is(notificationId)), NOTIFICATION_COLLECTION)) {
            return;
        }

        mongo.insert(notificationDocument(job, userId, notificationId), NOTIFICATION_COLLECTION);
        publishRealtime(userId, notificationId);
    }

    private boolean sellerUpdatesEnabled(String userId) {
        Document preference = mongo.findById(userId, Document.class, PREFERENCE_COLLECTION);
        return preference == null || preference.getBoolean("sellerUpdates", true);
    }

    private static Document notificationDocument(
            Document job, String userId, String notificationId) {
        return new Document("_id", notificationId)
                .append("userId", userId)
                .append("type", NOTIFICATION_TYPE)
                .append("title", "Cửa hàng bạn theo dõi vừa cập nhật")
                .append("message", "Thông tin cửa hàng vừa có thay đổi quan trọng.")
                .append("payload", job.get("payload"))
                .append("readAt", null)
                .append("createdAt", LocalDateTime.now());
    }

    private void publishRealtime(String userId, String notificationId) {
        kafka.publish(
                REALTIME_TOPIC,
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
                                "notificationId", notificationId,
                                "userId", userId,
                                "type", NOTIFICATION_TYPE)));
    }

    private void complete(Document job) {
        mongo.updateFirst(
                Query.query(Criteria.where("_id").is(job.get("_id"))),
                new Update().set("status", "COMPLETED").set("completedAt", LocalDateTime.now()),
                JOB_COLLECTION);
    }

    private void checkpoint(Document job, String lastUserId) {
        mongo.updateFirst(
                Query.query(Criteria.where("_id").is(job.get("_id"))),
                new Update()
                        .set("status", "RUNNING")
                        .set("cursor", lastUserId)
                        .set("updatedAt", LocalDateTime.now()),
                JOB_COLLECTION);
    }
}
