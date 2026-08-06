# Seller

## Production use cases
- onboarding.
- verification/suspension.
- shop profile.
- staff roles.
- warehouse ownership.
- listing association.
- dashboard.

## Business invariants
- seller-scoped user cannot access another seller.
- suspended seller restrictions propagate.
- privileged changes audited.

## Patterns / techniques
- **Aggregate**: applied only where the use case above needs it.
- **Policy**: applied only where the use case above needs it.
- **Outbox**: applied only where the use case above needs it.
- **CQRS-lite**: applied only where the use case above needs it.

## Persistence and concurrency

JPA seller/store/staff; projections for dashboard; ownership predicates in authorization ports.

## Failure and recovery

Reconcile propagated suspension; audit manual changes.

## Required tests
- tenant isolation.
- lifecycle.
- staff permission.
- event propagation.

## Completion rule

CRUD-only behavior is not sufficient. The context is complete only when the applicable invariants, idempotency/concurrency, failure recovery, observability and fresh verification evidence are implemented.
