# Use Case — Catalog publish, dynamic variants and checkout snapshot

## Problem
A product cannot be treated as a single CRUD row. Publishability depends on product state, variant validity and downstream media readiness, while checkout needs an immutable SKU snapshot.

## Constraints
- Concurrent editors must not silently overwrite each other.
- Variant combinations must be deterministic and duplicate-safe.
- Catalog write state must not be coupled to Search or Media databases.

## Candidate solutions
- Store free-form JSON and validate only in the UI — rejected because invariants disappear server-side.
- Generate SKUs ad hoc in controllers — rejected because combination rules become transport logic.

## Selected solution
ProductUseCase owns lifecycle orchestration, VariantGenerationUseCase delegates Cartesian generation to SkuCombinationGenerator, ProductPersistenceAdapter uses Spring Data JPA + @Version, and SkuSnapshotUseCase exposes a checkout-specific read projection. Publish emits source-owned outbox events only after media readiness is confirmed.

## Important failure modes
- Stale editor version -> optimistic-lock conflict.
- Media not ready -> publish rejected without changing product state.
- Duplicate variant values -> domain validation rejects the generation request.

## Main implementation references
- `backend/services/be-catalog-api/.../ProductUseCase.java`
- `backend/services/be-catalog-api/.../VariantGenerationUseCase.java`
- `backend/services/be-catalog-api/.../SkuSnapshotUseCase.java`

## Verification
- `ProductTest`
- `SkuCombinationGeneratorTest`

The JUnit files above are test sources. The final Java-25 Maven/Testcontainers execution remains a separate runtime acceptance gate and must not be inferred from source presence alone.
