# Use Case — Promotion targeting, stacking and checkout reservation

## Problem
Promotion eligibility depends on more than a coupon code; it must evaluate seller/SKU/category/channel/customer segment and reserve limited usage under concurrency.

## Constraints
- Global/per-customer limits must be race-safe.
- Stacking/exclusion must be deterministic.
- Checkout rollback must release only its own reservation.

## Candidate solutions
- Huge if/else in controller — rejected.
- Apply discount without reservation — rejected because concurrent checkouts can over-redeem.

## Selected solution
PromotionUseCase evaluates candidates through PromotionEngine; reservation use cases persist idempotent reserve/confirm/release state keyed by checkout. Priority and exclusion groups resolve conflicts deterministically.

## Important failure modes
- Usage limit reached -> typed limit exception.
- Second request with same checkout key -> same logical reservation.
- Downstream checkout failure -> compensation releases reservation.

## Main implementation references
- `backend/services/be-promotion-api/.../PromotionUseCase.java`
- `backend/services/be-promotion-api/.../PromotionCheckoutReservationUseCase.java`

## Verification
- `PromotionEngineTest`
- `PromotionTest`
- `verification/verify_checkpoint4_checkout_promotion.py`

The JUnit files above are test sources. The final Java-25 Maven/Testcontainers execution remains a separate runtime acceptance gate and must not be inferred from source presence alone.
