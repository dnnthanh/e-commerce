# Feature Spec 004 — Backend Deployable Process Topology

## Goal

Keep each bounded context independently scalable without forcing API traffic to compete with outbox polling, Kafka backlog processing, or long-running jobs.

## Naming

Every backend deployable uses `be-<context>-<role>`; Java package root is `com.dnnthanh.marketplace.be.<context>.<role>`.

Typical roles:

- `api`: synchronous HTTP;
- `worker`: inbound Kafka/background business work + Inbox/idempotency;
- `job`: schedulers/batch/reconciliation;
- `outbox`: transactional-outbox publisher.

## Rules

- Split per bounded context, not into one global job/outbox service.
- API, worker, job and outbox may share the bounded context database and internal modules but never another context's repositories/entities.
- Do not create every role when there is no workload/failure-isolation reason.
- Separate processes get independent CPU/memory, DB-pool sizing, consumer concurrency, HPA, probes, deployment and retry controls.
- Outbox publisher never runs inside the business transaction.
- Inbox is normally a table/pattern owned by the worker; a standalone inbox deployable requires an explicit spec.

## Inventory reference topology

```text
be-inventory-api
be-inventory-worker
be-inventory-job
be-inventory-outbox
```

## Verification cases

1. Create a synthetic large outbox backlog and prove API latency does not share the publisher executor/process.
2. Pause `be-inventory-outbox`; inventory API remains available and outbox backlog grows observably.
3. Restart publisher; backlog drains idempotently without duplicate business effects.
4. Run a heavy reservation-expiry job; API resource pool remains isolated.

See `usecase/deployable-isolation/`.
