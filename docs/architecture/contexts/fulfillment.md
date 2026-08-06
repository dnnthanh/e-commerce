# Shipping & Fulfillment

## Production use cases
- quote.
- warehouse split.
- shipment/waybill.
- pick-pack-ship.
- multi-package.
- partial shipment.
- carrier webhook.
- tracking/SLA.
- RTS.

## Business invariants
- carrier request key idempotent.
- tracking event cannot regress shipment.
- seller fulfillment completes only when all required packages delivered/cancelled legally.

## Patterns / techniques
- **Strategy**: applied only where the use case above needs it.
- **Adapter**: applied only where the use case above needs it.
- **State**: applied only where the use case above needs it.
- **Inbox/Outbox**: applied only where the use case above needs it.

## Persistence and concurrency

Relational shipment state + event timeline; carrier adapters outside transaction; inbox for callbacks.

## Failure and recovery

Retry carrier transient errors; reconcile out-of-order/provider drift; operations replay.

## Required tests
- state transitions.
- duplicate carrier callback.
- partial shipment.
- SLA/reconcile.

## Completion rule

CRUD-only behavior is not sufficient. The context is complete only when the applicable invariants, idempotency/concurrency, failure recovery, observability and fresh verification evidence are implemented.
