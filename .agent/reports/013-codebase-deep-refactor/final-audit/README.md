# Feature 013 — Final audit evidence

This directory is the stable final-audit index requested by the project rules. It separates **what exists in source** from **what was actually executed**.

- `usecase-coverage.md` — per-service application-use-case inventory, public operations, test-source count and remaining spec gaps.
- `test-evidence-index.md` — where unit tests/smokes/static gates live and what their evidence means.
- `final-regression.log` — fresh final source/static/domain/cross-context run.
- `java25-toolchain.log` — fresh Java/Maven/Docker availability evidence.
- `java25-usecase-evidence.log` — Java-25 alignment + usecase-documentation/evidence gate.

Historical reports under sibling checkpoint/final-service-depth directories are retained for traceability, but this directory is the current audit entry point.
