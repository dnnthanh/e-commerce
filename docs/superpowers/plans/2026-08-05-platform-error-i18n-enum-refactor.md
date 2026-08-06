# Platform Error/i18n/Enum Refactor Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make `be-platform-starter` a clean shared technical core with typed errors/i18n and type-safe enum JSON mapping while preserving API compatibility.

**Architecture:** Move shared platform types into responsibility packages; resolve errors through `ErrorCode -> MessageResolver -> ApiError`; keep bounded-context-specific machine codes in service enums. Business enums expose stable JSON codes and MapStruct maps enum-to-enum without `.name()` conversions.

**Tech Stack:** Java 25, Spring Boot 4.1, Lombok, MapStruct 1.6.3, Jackson 3, Spring MessageSource.

## Global Constraints

- Do not create `be-core`; `be-platform-starter` is the shared technical core.
- No business-domain sharing across services.
- No hard-coded API error code/message/status tuples in handlers/exceptions.
- Preserve current enum JSON values and database persistence semantics.
- Business rules must not live in MapStruct mapping code.

---

### Task 1: Regression quality gate
**Files:** Create `verification/verify_platform_core_i18n_enum.py`.
- [ ] Assert typed error/i18n contracts and package layout.
- [ ] Assert Comment responses use enum types and mapper has no `.name()`/`java(...)` enum conversion.
- [ ] Run on current source and capture RED.

### Task 2: Package and API-core cleanup
**Files:** Move platform API/context/jackson/security/trace/web/event classes to responsibility packages; update imports and package-sensitive verifiers.
- [ ] Move files and update package declarations/imports.
- [ ] Add Lombok constructor/getter annotations where they remove boilerplate.
- [ ] Re-run source-hygiene checks.

### Task 3: Typed exception + polymorphic i18n
**Files:** Create `CodeEnum`, `MessageResolvable`, `MessageResolver`, `SpringMessageResolver`, `ErrorCode`, `PlatformErrorCode`; refactor `BusinessException`, `GlobalExceptionHandler`, security handlers and service error enums.
- [ ] Replace handler string literals with `PlatformErrorCode`.
- [ ] Replace service `BusinessException` constructors with service error enums.
- [ ] Preserve localized messages and HTTP statuses.

### Task 4: Comment enum/mapping cleanup
**Files:** Comment enums/responses/mapper/model.
- [ ] Enum responses use enum types.
- [ ] Comment enum codes serialize through `@JsonValue` contract.
- [ ] Remove `.name()` enum conversion from MapStruct.
- [ ] Keep deleted-content rule out of mapper business logic.

### Task 5: Project enum code convention
**Files:** Remaining business/API enum sources.
- [ ] Add stable code contract without changing existing JSON values.
- [ ] Preserve existing custom enum behavior/fields.

### Task 6: Verification and packaging
- [ ] Run new regression gate.
- [ ] Run existing final/static audits.
- [ ] Attempt targeted Maven compile with available runtime and report any environment limitation honestly.
- [ ] Package a new ZIP and validate archive integrity.
