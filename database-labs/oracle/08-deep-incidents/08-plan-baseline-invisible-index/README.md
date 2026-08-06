# SQL plan management and invisible-index rollout

## Symptom
A candidate index improves one query but may hurt DML and alter many plans. Operators need a controlled canary and fallback plan.

The incident is intentionally data-shape dependent. Do not judge the query from SQL text alone. Reproduce both a hot key and a long-tail key before changing indexes or memory settings.

## Data shape and workload
Run `../00-bootstrap.sql` or seed an equivalent data set: millions of rows, a dominant hot seller/customer, a long tail, wide payload rows, and concurrent writers. Keep the skew; uniform random data makes this lab meaningless.

## Baseline procedure
1. Capture a cold-ish and warm-cache run.
2. Run `baseline.sql` with a hot parameter and at least three normal parameters.
3. Capture DBMS_XPLAN.DISPLAY_CURSOR(format=>'ALLSTATS LAST +PEEKED_BINDS +PARTITION +IOSTATS').
4. Record wall time, CPU, logical/physical IO, rows returned, rows read, and concurrency level.
5. Save the plan before adding indexes.

## Evidence to capture
Primary signals: invisible index session testing, SQL plan baseline acceptance, buffer gets, parse/plan changes and DML overhead.

Also correlate with V$SQL, V$SQL_PLAN_STATISTICS_ALL, V$SESSION, V$LOCK, AWR/ASH where licensed/available. Record database version and session-level settings because optimizer/memory behavior is version and configuration sensitive.

## Root cause hypotheses
- Cardinality/selectivity may be wrong because distribution is skewed or predicates are correlated.
- A plan can be locally optimal for one parameter and globally bad for another.
- A low-cost access path can still be expensive after multiplying by loops/lookups.
- Memory, locking, MVCC/undo/version-store, partition pruning, or storage layout may dominate even when an index exists.

Before changing anything, write down which hypothesis the captured evidence supports and which evidence would falsify it.

## Experiments
Run `solutions.sql` one candidate at a time. After each experiment, recapture the plan and metrics. Do not stack three fixes and then guess which one helped. Test hot/cold parameter distributions and concurrency, not one happy-path execution.

## Trade-offs
Every accepted fix must discuss:
- read latency p50/p95/p99 and worst hot-key latency;
- index/storage growth and cache footprint;
- INSERT/UPDATE/DELETE or refresh overhead;
- lock duration/concurrency impact;
- memory/TEMP/tempdb/PGA/undo implications;
- operational complexity, rollback path, and whether a plan hint/force creates future plan debt.

A fix that makes one SELECT fast but doubles write cost is not automatically a production win.

## Regression matrix
Run `regression.sql` and add a small table to your notes with: hot key, long-tail key, narrow window, wide window, empty result, concurrent writers, warm cache, and after-statistics-refresh cases. Record elapsed time, logical IO/buffer gets, rows read, plan identifier and write overhead.

## Acceptance criteria
- The root cause is supported by captured evidence, not intuition.
- Hot-key p95 improves materially without a severe long-tail regression.
- Estimate/actual or rows-read/rows-returned gaps are understood.
- No new blocking/deadlock/spill regression appears at expected concurrency.
- Index/storage/write amplification is measured and judged acceptable.
- The rollback command and production monitoring signal are documented.

## Production decision
Finish with one of `ACCEPT`, `REJECT`, or `TEMPORARY MITIGATION`. Explain why and state what metric would trigger reevaluation.
