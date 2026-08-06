# Use Case — Checkout Saga compensation and ambiguous payment

## Problem
Checkout spans pricing, promotion, inventory, payment and order. A local transaction cannot atomically commit all participants.

## Constraints
- Duplicate checkout submit must not duplicate side effects.
- Payment timeout is UNKNOWN, not automatic failure.
- Compensation must not undo a reservation after an Order already exists.

## Candidate solutions
- Distributed XA — rejected for cross-service/provider boundaries.
- Blind retries — rejected because POST/payment side effects may duplicate.

## Selected solution
CheckoutImplement + durable CheckoutProcess/CheckoutSaga implement a process manager with idempotency key, recorded progress, compensation and recovery worker. Promotion and Inventory are reserved first; failure before Order creation releases them; ambiguous payment remains recoverable.

## Important failure modes
- Inventory fails after promotion -> release promotion.
- Client loses response after order creation -> idempotency returns existing result.
- Payment UNKNOWN -> reconciliation/recovery path, no second charge.

## Main implementation references
- `backend/services/be-checkout-api/.../CheckoutImplement.java`
- `backend/services/be-checkout-api/.../CheckoutRecoveryUseCase.java`

## Verification
- `CheckoutOrchestratorTest`
- `CheckoutSagaTest`
- `CheckoutProcessTest`

The JUnit files above are test sources. The final Java-25 Maven/Testcontainers execution remains a separate runtime acceptance gate and must not be inferred from source presence alone.
