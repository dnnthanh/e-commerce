package com.dnnthanh.marketplace.be.notification.api.adapter.out.mongo;

import com.dnnthanh.marketplace.be.notification.api.application.port.out.NotificationInboxPort;
import com.dnnthanh.marketplace.be.notification.api.application.port.out.NotificationInboxReadPort;
import com.dnnthanh.marketplace.be.notification.api.application.query.NotificationInboxItem;
import com.dnnthanh.marketplace.be.notification.api.application.query.NotificationPreferenceSnapshot;
import com.dnnthanh.marketplace.be.notification.api.application.query.NotificationSearchCriteria;
import com.dnnthanh.marketplace.be.notification.api.domain.enumtype.NotificationChannel;
import com.dnnthanh.marketplace.be.notification.api.domain.model.NotificationDelivery;
import com.dnnthanh.marketplace.be.notification.api.domain.model.NotificationPreference;
import com.dnnthanh.marketplace.be.platform.stereotype.Persistence;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

/** Mongo source-of-truth adapter for durable inbox, preferences and delivery deduplication. */
@Persistence
@RequiredArgsConstructor
public class MongoNotificationPersistenceAdapter
        implements NotificationInboxPort, NotificationInboxReadPort {
    private static final String PREFERENCE_COLLECTION = "notification_preference";
    private static final String FOLLOW_COLLECTION = "seller_follow";

    private final MongoTemplate mongo;

    @Override
    public Optional<NotificationPreference> preference(String userId) {
        Document document = mongo.findById(userId, Document.class, PREFERENCE_COLLECTION);
        if (document == null) {
            return Optional.empty();
        }
        EnumSet<NotificationChannel> channels = EnumSet.of(NotificationChannel.IN_APP);
        if (document.getBoolean("email", false)) {
            channels.add(NotificationChannel.EMAIL);
        }
        if (document.getBoolean("push", false)) {
            channels.add(NotificationChannel.PUSH);
        }
        String quietFrom = document.getString("quietFrom");
        String quietUntil = document.getString("quietUntil");
        return Optional.of(
                new NotificationPreference(
                        userId,
                        channels,
                        quietFrom == null ? null : LocalTime.parse(quietFrom),
                        quietUntil == null ? null : LocalTime.parse(quietUntil)));
    }

    @Override
    public Optional<NotificationDelivery> findByDeduplicationKey(String key) {
        NotificationDeliveryDocument document =
                mongo.findOne(
                        Query.query(Criteria.where("deduplicationKey").is(key)),
                        NotificationDeliveryDocument.class);
        return Optional.ofNullable(document).map(this::toDomain);
    }

    @Override
    public Optional<NotificationDelivery> findDelivery(String notificationId) {
        return Optional.ofNullable(
                        mongo.findById(notificationId, NotificationDeliveryDocument.class))
                .map(this::toDomain);
    }

    @Override
    public List<NotificationInboxPort.DeliveryClaim> claimDue(LocalDateTime now, int limit) {
        int bounded = Math.max(1, Math.min(limit, 200));
        List<NotificationInboxPort.DeliveryClaim> claimed = new ArrayList<>(bounded);
        LocalDateTime leaseUntil = now.plusMinutes(2);
        for (int index = 0; index < bounded; index++) {
            String attemptKey = UUID.randomUUID().toString();
            Criteria ready =
                    new Criteria()
                            .orOperator(
                                    new Criteria()
                                            .andOperator(
                                                    Criteria.where("status")
                                                            .in(
                                                                    NotificationDelivery
                                                                            .DeliveryStatus.PENDING,
                                                                    NotificationDelivery
                                                                            .DeliveryStatus
                                                                            .FAILED_RETRYABLE),
                                                    new Criteria()
                                                            .orOperator(
                                                                    Criteria.where("nextAttemptAt")
                                                                            .is(null),
                                                                    Criteria.where("nextAttemptAt")
                                                                            .lte(now))),
                                    new Criteria()
                                            .andOperator(
                                                    Criteria.where("status")
                                                            .is(
                                                                    NotificationDelivery
                                                                            .DeliveryStatus
                                                                            .SENDING),
                                                    Criteria.where("nextAttemptAt").lte(now)));
            Query query =
                    Query.query(ready)
                            .with(Sort.by(Sort.Order.asc("nextAttemptAt"), Sort.Order.asc("_id")));
            Update claim =
                    new Update()
                            .set("status", NotificationDelivery.DeliveryStatus.SENDING)
                            .set("nextAttemptAt", leaseUntil)
                            .set("lastError", null)
                            .inc("attempts", 1)
                            .addToSet("providerAttemptKeys", attemptKey);
            NotificationDeliveryDocument document =
                    mongo.findAndModify(
                            query,
                            claim,
                            FindAndModifyOptions.options().returnNew(true),
                            NotificationDeliveryDocument.class);
            if (document == null) {
                break;
            }
            claimed.add(new NotificationInboxPort.DeliveryClaim(toDomain(document), attemptKey));
        }
        return List.copyOf(claimed);
    }

    @Override
    public NotificationDelivery save(NotificationDelivery delivery) {
        NotificationDeliveryDocument saved = mongo.save(toDocument(delivery));
        return toDomain(saved);
    }

    @Override
    public List<NotificationInboxItem> list(String userId, NotificationSearchCriteria criteria) {
        Criteria owner = Criteria.where("userId").is(userId);
        if (criteria.after() != null) {
            Criteria cursor =
                    criteria.afterId() == null
                            ? Criteria.where("createdAt").gt(criteria.after())
                            : new Criteria()
                                    .orOperator(
                                            Criteria.where("createdAt").gt(criteria.after()),
                                            new Criteria()
                                                    .andOperator(
                                                            Criteria.where("createdAt")
                                                                    .is(criteria.after()),
                                                            Criteria.where("_id")
                                                                    .gt(criteria.afterId())));
            owner = new Criteria().andOperator(owner, cursor);
        }
        Query query =
                Query.query(owner)
                        .with(Sort.by(Sort.Order.asc("createdAt"), Sort.Order.asc("_id")))
                        .limit(criteria.size());
        return mongo.find(query, NotificationDocument.class).stream()
                .map(
                        item ->
                                new NotificationInboxItem(
                                        item.id(),
                                        item.type(),
                                        item.title(),
                                        item.message(),
                                        item.payload(),
                                        item.readAt(),
                                        item.createdAt()))
                .toList();
    }

    @Override
    public long unreadCount(String userId) {
        return mongo.count(
                Query.query(Criteria.where("userId").is(userId).and("readAt").is(null)),
                NotificationDocument.class);
    }

    @Override
    public void markRead(String userId, String notificationId) {
        mongo.updateFirst(
                Query.query(Criteria.where("_id").is(notificationId).and("userId").is(userId)),
                new Update().set("readAt", LocalDateTime.now()),
                NotificationDocument.class);
    }

    @Override
    public void markAllRead(String userId) {
        mongo.updateMulti(
                Query.query(Criteria.where("userId").is(userId).and("readAt").is(null)),
                new Update().set("readAt", LocalDateTime.now()),
                NotificationDocument.class);
    }

    @Override
    public NotificationPreferenceSnapshot preferenceSnapshot(String userId) {
        Document document = mongo.findById(userId, Document.class, PREFERENCE_COLLECTION);
        if (document == null) {
            return new NotificationPreferenceSnapshot(true, false, false, true);
        }
        return new NotificationPreferenceSnapshot(
                document.getBoolean("realtime", true),
                document.getBoolean("email", false),
                document.getBoolean("push", false),
                document.getBoolean("sellerUpdates", true));
    }

    @Override
    public NotificationPreferenceSnapshot savePreference(
            String userId, NotificationPreferenceSnapshot preference) {
        mongo.upsert(
                Query.query(Criteria.where("_id").is(userId)),
                new Update()
                        .set("realtime", preference.realtime())
                        .set("email", preference.email())
                        .set("push", preference.push())
                        .set("sellerUpdates", preference.sellerUpdates())
                        .set("updatedAt", LocalDateTime.now()),
                PREFERENCE_COLLECTION);
        return preference;
    }

    @Override
    public void setSellerFollow(String userId, Long sellerId, boolean active) {
        mongo.upsert(
                Query.query(Criteria.where("userId").is(userId).and("sellerId").is(sellerId)),
                new Update()
                        .setOnInsert("userId", userId)
                        .setOnInsert("sellerId", sellerId)
                        .set("active", active)
                        .set("updatedAt", LocalDateTime.now()),
                FOLLOW_COLLECTION);
    }

    private NotificationDeliveryDocument toDocument(NotificationDelivery delivery) {
        return new NotificationDeliveryDocument(
                delivery.notificationId(),
                delivery.deduplicationKey(),
                delivery.userId(),
                delivery.channel(),
                delivery.providerAttemptKeys(),
                delivery.attempts(),
                delivery.status(),
                delivery.nextAttemptAt(),
                delivery.lastError());
    }

    private NotificationDelivery toDomain(NotificationDeliveryDocument document) {
        return NotificationDelivery.rehydrate(
                document.notificationId(),
                document.deduplicationKey(),
                document.userId(),
                document.channel(),
                document.providerAttemptKeys(),
                document.attempts(),
                document.status(),
                document.nextAttemptAt(),
                document.lastError());
    }
}
