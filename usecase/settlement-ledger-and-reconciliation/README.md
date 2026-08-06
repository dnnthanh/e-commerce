# Use Case — Immutable seller settlement ledger and reconciliation

## Problem
A mutable final seller balance cannot explain captures, commissions, refunds, adjustments or later-period corrections.

## Constraints
- Source event consumption is idempotent.
- Balance must be rebuildable from immutable entries.
- Period close/hold/dispute must not destroy history.

## Candidate solutions
- Update one balance row — rejected because reconciliation/audit is weak.
- Recompute from every upstream service on demand — rejected because it couples databases/services.

## Selected solution
SettlementEventUseCase appends immutable ledger effects, SettlementUseCase closes/holds/releases/rebuilds, and persistence keeps source-event identity for deduplication and reconciliation.

## Important failure modes
- Duplicate source event -> ignored.
- Refund in later period -> adjustment ledger entry.
- Rebuild discrepancy -> surfaced rather than overwritten invisibly.

## Main implementation references
- `backend/services/be-settlement-api/.../SettlementEventUseCase.java`
- `backend/services/be-settlement-api/.../SettlementUseCase.java`

## Verification
- `SellerSettlementLedgerTest`
- `database-labs/oracle`

The JUnit files above are test sources. The final Java-25 Maven/Testcontainers execution remains a separate runtime acceptance gate and must not be inferred from source presence alone.
