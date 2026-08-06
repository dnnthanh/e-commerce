# Use Case — Durable authorization provider mutation and recovery

## Problem
A Keycloak role/seller-scope mutation and the local audit/outbox write cannot share one transaction. Calling Keycloak first creates an unrecoverable gap when local persistence fails.

## Constraints
- Provider mutation must be traceable and retry-safe.
- Pending/failed changes need reconciliation.
- Seller scope and permission version must remain enforceable locally.

## Candidate solutions
- Provider call then local insert — rejected because successful provider change can become invisible locally.
- Wrap HTTP in DB transaction — rejected because it holds a connection and still is not atomic.

## Selected solution
AuthorizationUseCase records a durable change intent before invoking AuthorizationProviderPort, marks APPLIED/FAILED afterward, and exposes reconcilePending for incomplete mutations. AuthorizationPolicy enforces permission/seller scope/version freshness.

## Important failure modes
- Provider succeeds/local mark fails -> pending intent remains recoverable.
- Provider timeout -> FAILED/PENDING path, not silent loss.
- Cross-seller action -> policy rejects.

## Main implementation references
- `backend/services/be-authorization-api/.../AuthorizationUseCase.java`
- `backend/services/be-authorization-api/.../AuthorizationChangeRecorder.java`

## Verification
- `AuthorizationApplicationUseCaseTest`
- `AuthorizationPolicyTest`

The JUnit files above are test sources. The final Java-25 Maven/Testcontainers execution remains a separate runtime acceptance gate and must not be inferred from source presence alone.
