# Feature Spec 011 — Notification, Realtime and Fan-out

## Goal

Treat notification as a critical marketplace capability: persistent inbox, realtime UX, reliable async delivery, scalable fan-out and explicit failure recovery.

## Deployables

- `be-notification-api`: query inbox, unread/read state and user preferences.
- `be-notification-worker`: consume domain events, resolve audience, persist notifications and dispatch channels.
- `be-notification-realtime`: maintain realtime sessions and deliver persisted/created notifications to Angular.

MongoDB is the source of truth. Redis may hold unread counters/session routing but never replaces persisted notification history.

## Seller information change case

`be-seller-api` emits `SELLER_MATERIAL_INFO_CHANGED` through its transactional outbox only for customer-impacting changes. The worker targets relevant followers/subscribers/customers with active relationships. Large audiences are chunked/checkpointed and idempotent. A huge seller fan-out must not starve payment/order/normal notification events.

## System identity

Kafka workers build a non-human system `UserContext`. Internal private HTTP calls use Keycloak client credentials. Tests must prove this path without `@AuthenticationPrincipal`.

## Failure and recovery

- MongoDB unavailable -> Kafka retry/DLT; source business transaction is unaffected.
- realtime node unavailable -> persisted notification remains queryable and can deliver after reconnect.
- Keycloak/internal API unavailable -> controlled retry/DLT; no lost notification.
- duplicate event -> idempotent notification key prevents duplicate inbox row.
- large fan-out crash -> resume from persisted/checkpointed fan-out state.

## UI evidence

Screenshots must show realtime badge/inbox changes for order events and seller material-change events, plus offline-login recovery and mark-read behavior.
