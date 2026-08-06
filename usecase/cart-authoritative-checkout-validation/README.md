# Use Case — Cart authoritative checkout revalidation

## Problem
Cart price/product/stock snapshots become stale. Treating cached cart data as truth at checkout causes invalid prices, inactive products or oversell.

## Constraints
- Remote validation must not hold the cart DB transaction open.
- Concurrent browser tabs must not lose updates.
- Saved-for-later lines must never leak into checkout selection.

## Candidate solutions
- Trust cart snapshots — rejected as stale.
- Validate inside the cart DB transaction — rejected because remote latency holds connections.

## Selected solution
ShoppingCart keeps versioned local intent; CartCheckoutValidationImplement reads authoritative Catalog/Pricing/Inventory snapshots through ports outside the persistence transaction and returns typed validation issues such as PRICE_CHANGED and INSUFFICIENT_STOCK.

## Important failure modes
- Version mismatch -> CartVersionConflictException.
- Dependency timeout -> validation cannot claim success.
- Saved-for-later item -> excluded from checkout validation.

## Main implementation references
- `backend/services/be-cart-api/.../CartMutationUseCase.java`
- `backend/services/be-cart-api/.../CartCheckoutValidationImplement.java`

## Verification
- `ShoppingCartTest`
- `verification/src/CrossContextWorkflowSmoke.java`

The JUnit files above are test sources. The final Java-25 Maven/Testcontainers execution remains a separate runtime acceptance gate and must not be inferred from source presence alone.
