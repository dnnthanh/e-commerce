# Promotion

## Production use cases
- coupon/auto campaigns.
- eligibility.
- stacking/exclusion.
- usage reservation.
- confirm/release.
- simulation/explain.
- discount allocation.

## Business invariants
- usage limits never exceeded.
- same checkout cannot reserve twice.
- allocation sum equals awarded discount.

## Patterns / techniques
- **Specification**: applied only where the use case above needs it.
- **Composite**: applied only where the use case above needs it.
- **Strategy**: applied only where the use case above needs it.
- **Chain of Responsibility**: applied only where the use case above needs it.
- **Reservation**: applied only where the use case above needs it.

## Persistence and concurrency

JPA campaign/usage tables with atomic reservation/locking; outbox on reserve/confirm/release.

## Failure and recovery

Compensate reservation on checkout failure; expire abandoned reservations.

## Required tests
- eligibility/stacking tests.
- concurrent redemption.
- compensation test.

## Completion rule

CRUD-only behavior is not sufficient. The context is complete only when the applicable invariants, idempotency/concurrency, failure recovery, observability and fresh verification evidence are implemented.
