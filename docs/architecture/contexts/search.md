# Search & Discovery

## Production use cases
- incremental indexing.
- full reindex with checkpoint.
- faceted search.
- autocomplete.
- cursor/search-after.
- zero-result analytics.
- index alias swap.

## Business invariants
- older events cannot overwrite newer document.
- filter semantics are stable.
- deep pagination does not use unbounded offset.

## Patterns / techniques
- **CQRS-lite**: applied only where the use case above needs it.
- **Adapter**: applied only where the use case above needs it.
- **Idempotent Consumer**: applied only where the use case above needs it.
- **Strategy**: applied only where the use case above needs it.

## Persistence and concurrency

Search engine is read model; catalog/listing remains source of truth; event version guards stale writes.

## Failure and recovery

Degrade search explicitly; resume reindex; replay failed indexing events.

## Required tests
- query DSL contract tests.
- stale event test.
- reindex resume test.

## Completion rule

CRUD-only behavior is not sufficient. The context is complete only when the applicable invariants, idempotency/concurrency, failure recovery, observability and fresh verification evidence are implemented.
