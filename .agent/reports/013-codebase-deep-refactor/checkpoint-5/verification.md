# Feature 013 — Checkpoint 5 Verification

## Scope

Checkpoint 5 hardens Checkout integration boundaries:

- All remote service locations moved from Java literals into validated `checkout.remote.*`
  configuration properties backed by environment variables.
- Checkout remote adapters use separate `RestClient` instances per bounded context.
- Resilience4j CircuitBreaker + Bulkhead isolate Pricing, Promotion, Inventory, Order and Payment
  dependency failures; no blind retry policy was added for business failures.
- `spring-boot-starter-aop` and `resilience4j-spring-boot3` are declared explicitly.
- Checkout ArchUnit source rules enforce application->port boundaries, prevent controller->repository
  dependencies and keep domain code free from Spring/JPA dependencies.
- Checkout orchestration tests cover pre-Order compensation and the important opposite rule: once an
  Order exists, reservations are not silently released on a later dependency timeout.

## Evidence

TDD red evidence: `red.log` — 8 expected failures before implementation.

Fresh structural verification:

```text
python3 verification/verify_checkpoint5_resilience_architecture.py
Summary: 16 passed, 0 failed
```

`application.yml` and relevant POM XML are parsed in checkpoint 6 regression evidence.

## Not claimed

The ArchUnit/JUnit classes are source-complete but their Maven execution is BLOCKED by the current
runtime toolchain/network limitations recorded in checkpoint 6.
