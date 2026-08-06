# Use Case — Durable notification delivery, retry, DLQ and replay

## Problem
Email/push/SMS/realtime providers are unreliable. Delivery must not lose the durable inbox item or double-send when multiple worker instances compete.

## Constraints
- Durable inbox is source of truth.
- Only one worker may own a delivery lease.
- Retryable provider failure uses bounded backoff and eventually DLQ.

## Candidate solutions
- Send synchronously during source request — rejected.
- Realtime-only notification — rejected because disconnect loses data.

## Selected solution
NotificationDispatchUseCase prepares durable delivery work, NotificationDeliveryUseCase atomically claims due work and records attempts, NotificationReplayUseCase requeues dead letters, and realtime is emitted only as an acceleration path.

## Important failure modes
- Worker dies after claim -> lease expiry allows recovery.
- Provider repeatedly fails -> FAILED/DLQ state.
- Replay duplicate -> stable delivery id keeps side effect deduplicated.

## Main implementation references
- `backend/services/be-notification-api/.../NotificationDeliveryUseCase.java`
- `backend/services/be-notification-api/.../NotificationReplayUseCase.java`

## Verification
- `NotificationPreferenceTest`
- `NotificationContractTest`
- `usecase/notification-disconnect-reconnect/README.md`

The JUnit files above are test sources. The final Java-25 Maven/Testcontainers execution remains a separate runtime acceptance gate and must not be inferred from source presence alone.
