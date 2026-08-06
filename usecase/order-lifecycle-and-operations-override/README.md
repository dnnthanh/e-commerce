# Use Case — Marketplace Order lifecycle and guarded operations override

## Problem
Marketplace Order is a parent aggregate with seller sub-orders, immutable financial snapshots and asynchronous payment/fulfillment transitions. A generic status update endpoint is unsafe.

## Constraints
- Checkout creation is exactly-once.
- Illegal state transitions are rejected.
- Manual override requires explicit reason/compensation awareness.

## Candidate solutions
- Single mutable order status string — rejected.
- Direct operations SQL row fixes — rejected because audit/invariants are bypassed.

## Selected solution
OrderCommandUseCase drives MarketplaceOrder state transitions, seller cancellation, unpaid expiry and guarded manual override. Inbox deduplicates integration events; outbox records meaningful transitions; JPA @Version protects concurrent writes.

## Important failure modes
- Paid order customer cancel -> rejected.
- Duplicate payment/fulfillment event -> harmless Inbox dedupe.
- Manual override without required downstream compensation acknowledgement -> rejected.

## Main implementation references
- `backend/services/be-order-api/.../OrderCommandUseCase.java`
- `backend/services/be-order-api/.../OrderQueryUseCase.java`

## Verification
- `MarketplaceOrderTest`
- `verification/src/CrossContextWorkflowSmoke.java`

The JUnit files above are test sources. The final Java-25 Maven/Testcontainers execution remains a separate runtime acceptance gate and must not be inferred from source presence alone.
