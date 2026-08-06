# Authorization & Security

## Production use cases
- role/permission.
- resource ownership.
- seller tenant isolation.
- sensitive-operation hook.
- rate limit.
- security audit.

## Business invariants
- deny by default.
- role string not scattered in business code.
- cross-owner access impossible.

## Patterns / techniques
- **Policy**: applied only where the use case above needs it.
- **Specification**: applied only where the use case above needs it.
- **Adapter**: applied only where the use case above needs it.

## Persistence and concurrency

Authorization policies are domain/application components; data access always scoped; audit append-only.

## Failure and recovery

Stable forbidden errors; revoke/cache invalidation; fail closed on policy ambiguity.

## Required tests
- customer/seller/admin matrix.
- cross-seller isolation.
- revocation.

## Completion rule

CRUD-only behavior is not sufficient. The context is complete only when the applicable invariants, idempotency/concurrency, failure recovery, observability and fresh verification evidence are implemented.
