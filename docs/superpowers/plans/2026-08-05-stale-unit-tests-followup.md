# Stale Unit Tests Follow-up Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [x]`) syntax for tracking.

**Goal:** Align stale unit tests and recent compile follow-ups with the current domain/API contracts without adding compatibility overloads or fake production methods.

**Architecture:** Production domain APIs remain the source of truth. Tests are updated to exercise current invariants and lifecycle methods; simple missing imports are fixed at the owning type. Skipped modules are audited for test/source signature drift before packaging.

**Tech Stack:** Java 25, JUnit 5, Spring Boot 4.1, Maven multi-module.

## Global Constraints

- Do not add production overloads only to satisfy stale tests.
- Use domain-specific exception/status types in tests.
- Preserve current rich-domain lifecycle semantics.
- Include all simple patches reported after v9 in the new snapshot.

---

### Task 1: Repair reported stale tests
- [x] Update `PriceRuleTest` for current `PriceRule` constructor and `applies(PriceContext)`.
- [x] Update `PromotionTest` for current limits and `PromotionEvaluationContext`.
- [x] Update `ReturnRequestTest` for current line snapshot, receive/inspect/prepareRefund lifecycle.
- [x] Update `ShopTest` for `SellerStatus`, boundary normalization semantics, and `InvalidShopException`.

### Task 2: Repair recent compile follow-up
- [x] Add `java.util.Map` import to `NotificationView`.

### Task 3: Audit reactor modules skipped after return failure
- [x] Compare review/search/seller/settlement tests against current production APIs.
- [x] Verify outbox/worker modules have no stale test sources to update.

### Task 4: Verify and package
- [x] Run static signature regression checks for all modified tests.
- [x] Run repository quality gates available in this environment.
- [x] Create and integrity-check the new ZIP snapshot.
