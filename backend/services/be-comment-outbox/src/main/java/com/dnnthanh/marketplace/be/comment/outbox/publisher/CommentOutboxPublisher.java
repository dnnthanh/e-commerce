package com.dnnthanh.marketplace.be.comment.outbox.publisher;

import com.dnnthanh.marketplace.be.platform.event.DomainEvent;
import com.dnnthanh.marketplace.be.platform.event.OutboxEventFactory;
import com.dnnthanh.marketplace.be.platform.kafka.DomainEventProducer;
import com.dnnthanh.marketplace.be.platform.stereotype.Adapter;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;

/** Independently scalable MongoDB outbox publisher for durable comment events. */
@Adapter
@RequiredArgsConstructor
public class CommentOutboxPublisher {

    private final MongoTemplate mongo;

    private final DomainEventProducer kafka;

    private final OutboxEventFactory eventFactory;

    /** Publishes pending comment events in bounded batches. */
    @Scheduled(fixedDelayString = "${outbox.poll-ms:1000}")
    public void publishBatch() {
        Query query = Query.query(Criteria.where("status").is("PENDING")).limit(100);
        List<Document> events = mongo.find(query, Document.class, "comment_outbox");
        for (Document row : events) {
            String aggregateId = row.getString("aggregateId");
            DomainEvent event =
                    eventFactory.fromOutbox(
                            row.getString("eventId"),
                            row.getString("eventType"),
                            aggregateId,
                            row.getString("payloadJson"));
            kafka.publish("marketplace.comment.events", aggregateId, event)
                    .whenComplete(
                            (result, error) -> {
                                if (error == null) {
                                    mongo.updateFirst(
                                            Query.query(Criteria.where("_id").is(row.get("_id"))),
                                            new Update()
                                                    .set("status", "PROCESSED")
                                                    .set("processedAt", LocalDateTime.now()),
                                            "comment_outbox");
                                }
                            });
        }
    }
}
