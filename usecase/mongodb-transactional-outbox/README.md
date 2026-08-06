# MongoDB Transactional Outbox for Comment → Notification

## Problem

A product reply must update the Comment aggregate and eventually notify the root author. Publishing Kafka directly from the HTTP transaction creates a dual-write gap: MongoDB may commit while Kafka is unavailable, or Kafka may publish while MongoDB later aborts.

## Selected design

`be-comment-api` writes the reply, root-thread reference, and `comment_outbox` document in one MongoDB transaction. Local Docker uses a single-node Mongo replica set because Mongo transactions require a replica set. `be-comment-outbox` is a separately scalable process that publishes pending Outbox documents to Kafka and persists exponential-backoff retry state. `be-notification-worker` consumes the event with an atomic Inbox marker and creates the durable Mongo notification in the same transaction.

```text
HTTP reply
   ↓
Mongo transaction
   ├─ comment_reply
   ├─ comment_thread
   └─ comment_outbox(PENDING)
         ↓ commit
be-comment-outbox
         ↓ Kafka
marketplace.comment.replied.v1
         ↓
be-notification-worker
         ↓ Mongo transaction
   ├─ notification_inbox
   └─ notification
         ↓ after commit
realtime delivery
```

## Why realtime is after commit

Publishing realtime before Mongo commit can show a notification that later disappears if the transaction rolls back. `CreateNotificationService` therefore registers realtime delivery with transaction synchronization and publishes only after a successful commit. Realtime failure never deletes durable notification truth.

## Crash scenarios

- Kafka down: Comment request still succeeds; Outbox remains `PENDING` and retries later.
- Comment Outbox process crashes after Kafka publish but before marking `PUBLISHED`: Kafka may redeliver; Notification Inbox suppresses duplicate business effects.
- Notification worker crashes after Inbox claim but before notification save: both are in one Mongo transaction, so the claim rolls back and the event is safe to retry.
- Same author replies to own thread: event is still claimed idempotently, but no user notification is created.

## Scaling

`be-comment-api` and `be-comment-outbox` scale independently. Heavy Kafka recovery cannot consume request threads from the Comment API. Notification consumers have their own consumer group and retry topic/DLT policy.
