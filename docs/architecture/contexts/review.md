# Review & Moderation

## Production use cases
- verified purchase review.
- edit window/history.
- seller response.
- helpful reaction.
- report/moderation.
- rating projection.
- spam policy.

## Business invariants
- eligibility enforced.
- one active review per configured key.
- reaction/report idempotent.
- hidden review handled consistently.

## Patterns / techniques
- **Policy**: applied only where the use case above needs it.
- **Chain of Responsibility**: applied only where the use case above needs it.
- **Outbox**: applied only where the use case above needs it.
- **CQRS-lite**: applied only where the use case above needs it.

## Persistence and concurrency

Review source + moderation records; event-driven rating projection; atomic reaction counters.

## Failure and recovery

Moderation replay/dedupe; projection rebuild; spam provider degradation explicit.

## Required tests
- eligibility.
- duplicate review.
- concurrent helpful reaction.
- moderation projection.

## Completion rule

CRUD-only behavior is not sufficient. The context is complete only when the applicable invariants, idempotency/concurrency, failure recovery, observability and fresh verification evidence are implemented.
