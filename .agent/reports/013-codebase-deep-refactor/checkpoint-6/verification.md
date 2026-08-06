# Feature 013 — Checkpoint 6 Verification

## Scope

Checkpoint 6 implements a crash-recoverable outbox reference pattern for the Order (SQL Server) and
Payment (PostgreSQL) bounded contexts.

### Multi-instance claiming

- Payment uses `FOR UPDATE SKIP LOCKED` with an atomic CTE update/returning claim.
- Order uses SQL Server `UPDLOCK`, `READPAST`, and `ROWLOCK` with `UPDATE ... OUTPUT`.
- Claimed rows enter `PUBLISHING` with `locked_until` and incremented `attempt_count`.
- A worker crash leaves a lease that becomes reclaimable after expiry instead of permanently losing
  the event.

### Broker outcome handling

- Kafka acknowledgement marks the row `PROCESSED` and clears the lease.
- Failed sends return the row to `PENDING` with capped exponential backoff.
- The tenth failed attempt moves the row to terminal `FAILED`, retaining `last_error` for operations.
- If a process crashes after broker send but before marking processed, the event can be sent again;
  downstream Inbox/idempotent-consumer semantics remain the required duplicate-safety boundary.

### Schema

Both Order and Payment outboxes add:

- `attempt_count`
- `locked_until`
- `next_attempt_at`
- `last_error`

`OutboxRetryPolicy` is shared through the platform starter instead of duplicating backoff arithmetic.

## Fresh evidence

```text
python3 verification/verify_checkpoint6_outbox_recovery.py
Summary: 21 passed, 0 failed

javac ... OutboxRetryPolicy.java OutboxRetryPolicySmoke.java
java ... OutboxRetryPolicySmoke
OUTBOX_RETRY_POLICY_SMOKE=PASS
```

Full non-Maven regression evidence is stored in `regression.log` and ends with:

```text
ALL_NON_MAVEN_GATES=PASS
```

The regression includes checkpoint 4/5/6 gates, Feature 013 quality/depth/active-use-case gates,
runtime-flow verification, production-depth domain smoke, cross-context workflow smoke, YAML parsing,
POM XML parsing and pure-Java compilation.

## Toolchain blocker — fresh

See `toolchain.log`:

```text
java: OpenJDK 21.0.10
javac: 21.0.10
./mvnw: exit 6, DNS cannot resolve repo.maven.apache.org
docker: command not found, exit 127
```

Therefore these final acceptance items remain BLOCKED rather than PASS:

- Java 25 Maven reactor compilation.
- Spotless execution through Maven.
- JUnit/ArchUnit/Awaitility/Testcontainers execution.
- Docker Compose startup.
- Real database/Kafka/OpenSearch integration smoke.

Feature 013 remains `Implementation in progress`.
