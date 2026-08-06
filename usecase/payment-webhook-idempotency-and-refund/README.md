# Use Case — Payment callback idempotency, UNKNOWN recovery and cumulative refund

## Problem
Provider callbacks can be duplicated/out-of-order and client/provider timeouts create ambiguous results. Refunds may be partial and repeated over time.

## Constraints
- Never double-capture or over-refund.
- UNKNOWN must be reconciled rather than blindly retried.
- Provider-specific transport stays outside the domain model.

## Candidate solutions
- Treat timeout as FAILED — rejected because provider may have charged.
- Store each callback as the payment state — rejected because event order is not guaranteed.

## Selected solution
A single Payment aggregate handles attempts, provider facts and cumulative refund invariants. PaymentCallbackUseCase claims provider events idempotently; PaymentImplement reconciliation queries the provider; RefundUseCase caps cumulative refunded amount.

## Important failure modes
- Duplicate callback -> one business effect.
- Out-of-order callback -> illegal/stale transition ignored or rejected.
- Refund above captured amount -> InvalidRefundAmountException.

## Main implementation references
- `backend/services/be-payment-api/.../PaymentCallbackUseCase.java`
- `backend/services/be-payment-api/.../PaymentImplement.java`
- `backend/services/be-payment-api/.../RefundUseCase.java`

## Verification
- `PaymentTest`
- `usecase/payment-unknown-operations-recovery/README.md`

The JUnit files above are test sources. The final Java-25 Maven/Testcontainers execution remains a separate runtime acceptance gate and must not be inferred from source presence alone.
