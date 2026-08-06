# Feature Spec 000 — Platform Foundation

## Status

Design candidate. Implementation starts only after architecture review and a dedicated implementation plan.

## Problem

All services need consistent cross-cutting behavior. Copy/pasting logging, trimming, error mapping, Kafka conversion, tracing, scheduler context, and testing utilities would create drift and pollute business use cases.

## Scope

- backend build/BOM conventions;
- shared technical modules with strict no-domain rule;
- structured HTTP request/response logging with masking;
- global String trim with opt-out;
- error/i18n contract;
- OpenTelemetry/MDC integration;
- typed Kafka SerDe/event envelope conventions;
- system execution context abstraction for scheduler/Kafka;
- test utilities/ArchUnit baseline;
- Docker image conventions.

## Design principles

- Infrastructure concern belongs in configuration/filter/adapter, not business use case.
- Shared modules expose narrow technical contracts, never service domain entities.
- All defaults are overrideable per service where a valid use case exists.
- Security-sensitive logging defaults to conservative masking.

## Acceptance criteria

- A sample service can receive a trimmed request without manual `.trim()`.
- `@NoTrim`-equivalent field preserves whitespace.
- Request/response logs contain traceId and mask configured secrets.
- Error code is stable while message changes by locale.
- Kafka producer sends a typed event and consumer receives the typed event without manual `byte[]` parsing.
- Kafka/scheduler execution creates trace + non-human system context.
- ArchUnit prevents representative forbidden dependencies.
- Unit/integration tests prove each foundation behavior.

## Failure considerations

- Logging/telemetry export failure does not break business response.
- Malformed Kafka event has explicit deserialization/DLT policy.
- Missing locale falls back deterministically.
- Oversized request/response logging is truncated/skipped based on policy.

## Verification evidence

Store final evidence under `.agent/reports/000-platform-foundation/`.
