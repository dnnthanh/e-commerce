# Feature Spec 009 — Payment Providers: MoMo, VNPAY and VietQR

## Goal

Provide a production-style provider-neutral payment domain with real test/sandbox adapters plus deterministic local Compose simulators.

## Adapters

- `MoMoPaymentAdapter`
- `VnPayPaymentAdapter`
- `VietQrPaymentAdapter`

All implement one payment provider port and map provider payload/statuses into the internal payment state machine.

## Environments

Default `docker compose up -d --build` uses local provider simulators so the whole marketplace is reproducible without secrets or public callback exposure. Optional sandbox configuration enables external test endpoints and injects credentials from environment/secret files.

## Required behavior

- create/initiate payment;
- verified redirect/callback/webhook processing where provider supports it;
- query transaction status;
- UNKNOWN/PENDING_RECONCILIATION handling;
- duplicate request/webhook idempotency;
- signature/checksum validation;
- partial/full refund where supported;
- timeout/retry/backoff/circuit breaker policy based on operation safety;
- audit and trace correlation;
- no provider credential in log/audit.

VietQR QR generation is a presentation/payment-instruction step, not confirmation of funds. Confirmation is a separate strategy and state transition.

## Test evidence

- successful provider flows;
- invalid signature;
- duplicate callback/webhook;
- timeout after possible provider success -> UNKNOWN -> status query/reconciliation;
- provider temporary outage -> circuit breaker -> recovery;
- refund duplicate protection;
- local simulator and external sandbox configuration contract tests.
