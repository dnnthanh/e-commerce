# Feature 013 — Checkpoint 2 Verification

## Status

Checkpoint 2 implements production-depth domain behavior across the major bounded contexts and adds
cross-context race/compensation/idempotency smoke scenarios. Feature 013 remains **IN PROGRESS**;
the final acceptance gate is intentionally not marked complete.

## Fresh verification executed

### Static Feature 013 verifier

```bash
python verification/verify_feature_013_depth.py
```

Result: **59 passed, 0 failed**. See `static-verification.log`.

### Production-depth domain + cross-context smoke

```bash
./verification/verify_production_depth_smokes.sh
```

Result:

```text
PRODUCTION_DEPTH_DOMAIN_SMOKE=PASS
CROSS_CONTEXT_WORKFLOW_SMOKE=PASS
```

The script compiles only the source dependency closure required by the two smoke programs using
`javac -sourcepath`; this avoids falsely pulling unrelated legacy classes into the domain harness.

## Scenarios covered by executable smoke verification

- effective-price precedence;
- promotion eligibility/exclusion;
- inventory idempotent reservation and oversell prevention;
- cart optimistic version + guest merge;
- checkout compensation after ambiguous payment;
- payment callback deduplication, race behavior and cumulative refund protection;
- multi-package fulfillment and partial return;
- seller settlement ledger adjustment;
- stale search index event rejection;
- notification quiet-hours/deduplication;
- seller lifecycle;
- media processing state/idempotency;
- concurrent last-unit inventory reservation;
- outbox redelivery with downstream inbox deduplication.

## Environment blocker for the final gate

Current execution environment contains OpenJDK/Javac 21 only. Maven, Maven Wrapper and Docker are
not available. The repository target remains Java 25 as required by the approved spec.

Therefore this checkpoint **does not claim**:

- Java 25 full reactor compilation;
- `mvn clean verify`;
- Spotless execution over the full repository;
- JUnit/Testcontainers execution through Maven;
- Spring application-context startup;
- Docker Compose startup/API smoke tests.

See `environment.log` for fresh environment evidence.

## Known legacy gaps still blocking Feature 013 completion

`legacy-gap-inventory.log` inventories remaining pre-existing/legacy code that still violates the
final quality gate, including manual `.trim()`, generic business exceptions and compressed one-line
Java sources. These are explicitly retained as open work; checkpoint 2 must not be interpreted as a
claim that the repository-wide migration is complete.

## Artifacts introduced in checkpoint 2

- production-depth domain aggregates/policies/enums/exceptions for fulfillment, pricing, promotion,
  inventory, cart, checkout, payment, return, review, seller, search, notification, settlement,
  catalog, media, authorization, audit and operations;
- application use cases/ports for the corresponding flows, conditionally activated until concrete
  adapters are wired;
- JUnit test sources for the new domain behavior;
- `ProductionDepthDomainSmoke` and `CrossContextWorkflowSmoke`;
- `verify_production_depth_smokes.sh`;
- Feature 013 static verifier;
- architecture matrix mapping context -> use case -> pattern/invariant -> persistence/concurrency ->
  verification.
