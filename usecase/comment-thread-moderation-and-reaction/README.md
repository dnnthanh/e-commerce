# Use Case — Mongo comment thread, moderation and concurrent reaction

## Problem
Comment threads need replies, soft deletion, moderation, duplicate-safe reports/reactions and durable notification events while retaining MongoDB as source of truth.

## Constraints
- Deleting parent cannot destroy visible descendants.
- Reaction/report retry must be harmless.
- Realtime notification cannot become source of truth.

## Candidate solutions
- Hard delete whole subtree — rejected.
- Store reaction count only without per-user reaction — rejected because idempotency cannot be proven.

## Selected solution
CommentCommandUseCase handles create/reply/edit/delete/hide/report/reaction behind Mongo persistence port; rate limiting and mention extraction are dedicated components; Mongo outbox persists notification-worthy changes with the domain mutation.

## Important failure modes
- Parent soft-deleted -> descendants remain queryable.
- Duplicate report/reaction -> no duplicate business effect.
- Rate limit exceeded -> typed domain/application error.

## Main implementation references
- `backend/services/be-comment-api/.../CommentCommandUseCase.java`
- `backend/services/be-comment-api/.../CommentQueryUseCase.java`

## Verification
- `CommentThreadTest`
- `verification/src/ProductionDepthDomainSmoke.java`

The JUnit files above are test sources. The final Java-25 Maven/Testcontainers execution remains a separate runtime acceptance gate and must not be inferred from source presence alone.
