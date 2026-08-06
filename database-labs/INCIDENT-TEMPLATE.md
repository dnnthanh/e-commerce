# Incident title

## Symptom / SLO
State endpoint/job/query, production impact, p95/p99 or throughput target, affected tenant/seller/date range and when the regression began.

## Data shape
Record row counts, hot-key distribution, long-tail distribution, correlation, row width/LOB/JSON, active-vs-history ratio, partition count, write rate and retention window.

## Baseline
Capture actual plan, parameters/binds, session settings, elapsed/CPU, logical/physical IO, rows read/returned, estimate/actual rows, memory/spill, lock/wait and storage sizes.

## Evidence
List concrete plan nodes/DMVs/views that support or contradict each hypothesis. Mark the first point where row estimates or work volume diverge.

## Root cause hypothesis
Write one primary hypothesis and one falsifying observation. Separate symptom from cause.

## Candidate A
Describe the smallest change, expected plan difference, operational risk and rollback command.

## Candidate B
Describe an alternative such as query rewrite, statistics, read-model/materialization, pagination contract, partitioning or transaction-scope change.

## Trade-off
Quantify read benefit, storage/index size, write amplification, memory/temp/undo/version-store cost, concurrency/blocking risk and operational complexity.

## Regression matrix
Hot key, long-tail key, empty result, narrow/wide time range, concurrent writers, cold/warm cache, after statistics refresh, and after realistic data growth.

## Acceptance criteria
Define measurable p95/p99/throughput/IO/wait targets, no-regression thresholds, storage/write budget and monitoring signal.

## Production decision
`ACCEPT`, `REJECT`, or `TEMPORARY MITIGATION`, plus rollout/canary/rollback and reevaluation trigger.
