# Payment UNKNOWN → Operations Recovery Loop

## Problem

A timeout does not prove that a payment failed. Submitting another charge can double-charge the customer, while keeping `UNKNOWN` forever leaves the order and inventory Saga unresolved.

## Selected design

1. `be-payment-job` queries provider status; provider HTTP is deliberately outside a database transaction.
2. A short transactional updater applies only the returned provider fact and writes Payment Outbox events.
3. `payment_reconciliation_state` stores consecutive unresolved attempts. It is job/recovery state, not Payment aggregate business state.
4. After three consecutive unresolved attempts, Payment emits one stable incident id `payment-unknown:<paymentId>` to `marketplace.operations.incident.v1`.
5. `be-operations-worker` stores the incident. Admin requests `RECONCILE`; Operations writes the recovery command to its own Outbox.
6. `be-payment-worker` receives only commands targeted to `be-payment-worker`, re-queries the provider, and atomically emits a recovery-outcome event with any Payment state change.
7. Operations resolves the incident on a stable provider outcome or reopens it with the latest safe failure message.

## Important reliability properties

- No blind charge retry.
- Provider HTTP does not hold a local DB transaction/connection.
- Incident id is deterministic, so repeated automatic reporting cannot create multiple operator incidents.
- Recovery command uses source-owned handler `recoveryTarget`, separate from the process that originally reported the incident.
- A provider-confirmed `FAILED` state is a successful **recovery** because ambiguity has been removed.
- Automatic reconciliation can also resolve a previously reported incident without waiting for an admin click.

## Performance lab hook

The baseline schema intentionally has no optional index for `payment.status` or recovery-state analysis. Large seed data lives under `data/seed-large/postgresql/payment.sql`; execution-plan/index work belongs in the separate database lab.
