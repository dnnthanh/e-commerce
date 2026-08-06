# Use Case — Partial return, inspection, dispute and inventory disposition

## Problem
Return/refund must operate on selected quantities and immutable order allocations, not a whole-order boolean. Inspection may partially accept items and downstream refund/inventory updates can fail independently.

## Constraints
- Returned quantity/refund cannot exceed purchased allocation.
- Refund is idempotent.
- Inspection disposition is explicit: RESTOCK, QUARANTINE or SCRAP.

## Candidate solutions
- Separate ReturnCase and ReturnRequest aggregates — rejected after dual-path audit.
- Recompute refund from current catalog price — rejected because current price is not the sale contract.

## Selected solution
ReturnUseCase is the canonical workflow for create/approve/reject/receive/inspect/dispute/resolve/refund. ReturnRequest carries line quantities and disposition; refund calculation uses immutable Order snapshot allocation.

## Important failure modes
- Duplicate/overlapping return exceeds remaining quantity -> rejected.
- Refund succeeds but downstream state update fails -> recovery remains diagnosable/idempotent.
- Inspection rejects line -> no restock/refund for rejected quantity.

## Main implementation references
- `backend/services/be-return-api/.../ReturnUseCase.java`
- `backend/services/be-return-api/.../ReturnRequest.java`

## Verification
- `ReturnRequestTest`
- `verification/src/ReturnDomainRefactorSmoke.java`

The JUnit files above are test sources. The final Java-25 Maven/Testcontainers execution remains a separate runtime acceptance gate and must not be inferred from source presence alone.
