# Comment & Discussion

## Production use cases
- root/reply.
- edit window.
- soft delete descendants.
- hide/unhide.
- report.
- reaction.
- mention.
- rate limit.
- cursor thread read.

## Business invariants
- descendants preserved after parent soft delete.
- duplicate report/reaction harmless.
- depth policy enforced.
- blocked actor cannot mutate.

## Patterns / techniques
- **Aggregate**: applied only where the use case above needs it.
- **Policy**: applied only where the use case above needs it.
- **Adapter**: applied only where the use case above needs it.
- **Outbox/Inbox**: applied only where the use case above needs it.
- **Atomic Mongo Update**: applied only where the use case above needs it.

## Persistence and concurrency

Mongo source of truth; transaction for comment+outbox when supported; atomic update/version for reactions/edits.

## Failure and recovery

Duplicate events deduped; moderation/audit durable; hot-thread projection rebuildable.

## Required tests
- delete-parent-active-child.
- concurrent reactions.
- duplicate reports.
- retry create/reply.

## Completion rule

CRUD-only behavior is not sufficient. The context is complete only when the applicable invariants, idempotency/concurrency, failure recovery, observability and fresh verification evidence are implemented.
