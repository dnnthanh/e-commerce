# Feature 013 — Checkpoint 4 Verification

## Scope

Checkpoint 4 closes a real Checkout/Promotion workflow gap:

- Promotion evaluation is no longer a read-only `discount(...)` call from Checkout.
- Checkout reserves promotion usages through an authenticated internal API using the Checkout
  idempotency key.
- Promotion reservation returns the exact applied promotion identifiers and discount snapshot.
- Checkout confirms reserved promotion usage after Order creation.
- Checkout releases promotion reservations when Inventory/Order creation fails before an Order exists.
- Promotion usage limit metadata is loaded from persisted promotion conditions and enforced by the
  existing concurrency-safe usage adapter.
- Partial reservation failure compensates already-reserved promotions before propagating the error.

## Evidence

TDD red evidence: `promotion-integration-red.log` — 11 expected failures before implementation.

Fresh structural verification:

```text
python3 verification/verify_checkpoint4_checkout_promotion.py
Summary: 16 passed, 0 failed
```

The repository-wide non-Maven regression gate in checkpoint 6 also re-runs this verification.

## Not claimed

JUnit/Maven integration execution is not claimed because the environment cannot bootstrap Maven and
only provides JDK 21 while the project targets Java 25.
