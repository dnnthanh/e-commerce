# Use Case — Verified-purchase Review ownership and moderation

## Problem
Reviews need purchase eligibility, ownership, edit rules, helpful reaction idempotency and moderation without recalculating aggregate ratings synchronously.

## Constraints
- Only eligible purchaser may create.
- Helpful/report actions must be duplicate-safe.
- Rating projection is eventually consistent.

## Candidate solutions
- Trust client purchase flag — rejected.
- Recalculate all product reviews on each write — rejected for scaling.

## Selected solution
Review worker materializes verified-purchase lines from delivered orders; ReviewUseCase enforces ownership/edit/delete/helpful semantics; ReviewModerationUseCase controls visibility and outbox events update read/search projections.

## Important failure modes
- Unverified purchaser -> ReviewPermissionException.
- Duplicate helpful -> one logical reaction.
- Moderation hides content without destroying history.

## Main implementation references
- `backend/services/be-review-api/.../ReviewUseCase.java`
- `backend/services/be-review-api/.../ReviewModerationUseCase.java`
- `backend/services/be-review-worker/.../VerifiedPurchaseMaterializer.java`

## Verification
- `ProductReviewTest`

The JUnit files above are test sources. The final Java-25 Maven/Testcontainers execution remains a separate runtime acceptance gate and must not be inferred from source presence alone.
