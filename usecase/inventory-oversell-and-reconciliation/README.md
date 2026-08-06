# Use Case — Inventory reservation, oversell prevention and reconciliation

## Problem
Read-then-write stock mutation races under contention and eventually diverges from reservation/ledger state when retries or external syncs occur.

## Constraints
- Stock must never become negative.
- Reserve is idempotent by business operation key.
- Expired reservations must release reserved quantity exactly once.

## Candidate solutions
- JPA read-modify-write only — rejected for the high-contention mutation path.
- Global JVM lock — rejected because instances scale horizontally.

## Selected solution
Inventory keeps ordinary reads behind ports while contention-sensitive reserve/confirm/release uses atomic/native SQL and row locking. Transfer/adjustment write ledger events; InventoryReconciliationUseCase compares current balances against ledger/reservations.

## Important failure modes
- Last-unit race -> only one reservation wins.
- Duplicate reservation event -> no duplicate stock effect.
- Expiry worker crash -> SKIP LOCKED batch can be retried.

## Main implementation references
- `backend/services/be-inventory-api/.../InventoryReservationUseCase.java`
- `backend/services/be-inventory-api/.../InventoryReconciliationUseCase.java`
- `backend/services/be-inventory-job/.../ReservationExpiryJob.java`

## Verification
- `StockLedgerTest`
- `InventoryBalanceTest`
- `verification/src/InventoryDomainRefactorSmoke.java`

The JUnit files above are test sources. The final Java-25 Maven/Testcontainers execution remains a separate runtime acceptance gate and must not be inferred from source presence alone.
