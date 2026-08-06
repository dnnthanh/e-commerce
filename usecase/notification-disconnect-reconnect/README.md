# Notification disconnect / reconnect

## Problem
Realtime delivery is opportunistic. A browser, mobile device, proxy, gateway, or the realtime notification process can disconnect at any time. Durable notification state must not depend on the connection.

## Design
1. `be-notification-worker` consumes business events idempotently and persists notifications in MongoDB first.
2. Realtime delivery happens only after the durable write commits.
3. Realtime frames carry `notificationId`, type, and timestamp; MongoDB remains the source of truth.
4. The client persists the latest acknowledged cursor locally.
5. On reconnect, the client first calls the authenticated catch-up API with that cursor and loads missed notifications, then reopens the realtime channel.
6. Unread count is reconciled from durable state instead of trusting socket-local counters.
7. Duplicate frames are harmless because UI de-duplicates by `notificationId`.

## Failure cases
- Offline for hours: persisted notifications are returned after login/reconnect.
- Realtime process dies after DB commit: no data loss; catch-up recovers the notification.
- Kafka redelivery: Inbox/idempotency prevents duplicate notification documents.
- Reconnect storm: exponential backoff + jitter and gateway rate limiting.
- Access token expires: obtain a refreshed token through the normal auth flow before reconnecting; never place bearer tokens in the URL.
