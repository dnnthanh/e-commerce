# Use Case — Search projection stale-event protection and reindex

## Problem
Kafka redelivery/out-of-order catalog events can overwrite a newer OpenSearch document; index schema changes also need rebuild/swap without stopping writes.

## Constraints
- Older source version never overwrites newer projection.
- Search failure must not block checkout/order.
- Deep result sets use cursor/search-after rather than OFFSET.

## Candidate solutions
- Last event wins regardless of version — rejected.
- Cross-service DB joins for search — rejected.

## Selected solution
SearchIndexVersion guards source-version monotonicity, SearchProjectionConsumer materializes catalog/review changes, and ProductSearchUseCase exposes filtered cursor search. Reindex remains an asynchronous projection concern with versioned index assets/checkpoints.

## Important failure modes
- Duplicate/older event -> ignored.
- OpenSearch unavailable -> search degrades without corrupting source state.
- Invalid cursor -> typed search criteria error.

## Main implementation references
- `backend/services/be-search-api/.../ProductSearchUseCase.java`
- `backend/services/be-search-worker/.../SearchProjectionConsumer.java`

## Verification
- `SearchIndexVersionTest`
- `verification/src/SearchCursorSmoke.java`

The JUnit files above are test sources. The final Java-25 Maven/Testcontainers execution remains a separate runtime acceptance gate and must not be inferred from source presence alone.
