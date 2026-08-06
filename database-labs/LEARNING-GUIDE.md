# DBA Learning Guide

## How to work one incident

1. Read only **Symptom** and **Data shape** first. Do not read solutions.
2. Run the engine bootstrap and confirm skew/volume with row-count queries.
3. Execute `baseline.sql` for a hot parameter and several long-tail parameters.
4. Save actual execution-plan evidence and wait/IO/memory statistics.
5. Write a root-cause hypothesis with one falsifying observation.
6. Open `solutions.sql`; apply one candidate only.
7. Re-run the same evidence capture and compare plan operators, cardinality, IO, memory, lock time and writes.
8. Run `regression.sql` under multiple parameter shapes/concurrency levels.
9. Measure storage and DML overhead introduced by the fix.
10. Finish with a production decision and rollback/monitoring plan.

## Questions that distinguish a deep analysis from a basic one

- Is parent-node elapsed time inclusive of children? Should node durations actually be summed?
- Is the expensive node slow because each loop is slow, or because it executes many loops?
- Where does the first large estimated/actual row divergence appear?
- Is a scan type the root cause, or only the consequence of bad cardinality/selectivity?
- Did a covering index avoid row/heap lookups, and what did that do to write amplification?
- Is memory spill caused by a bad estimate, a genuinely large working set, or excessive concurrency?
- Does the proposed key order support equality/range/order-by together, and which optional predicates break that shape?
- Can partition pruning/elimination be proven in the plan, or is partitioning only present in DDL?
- Will a hot seller/customer produce a different optimal plan than the long tail?
- What happens after statistics refresh, restart/cold cache, schema growth, or a deployment that changes parameterization?
- What is the failure mode of the fix: deadlock, OOM, tempdb/TEMP pressure, purge/undo lag, plan debt, write latency, or operational maintenance complexity?


## Plan-reading vocabulary that every lab must use

Across engines, explicitly distinguish **EXPLAIN** estimates from runtime evidence. In PostgreSQL,
prove whether an **index-only** scan really avoids heap fetches by checking the visibility map/heap
fetch count; do not infer it from the operator name alone. Track column **correlation** and
multicolumn dependencies when cardinality is wrong: a selective-looking predicate can still produce
a poor plan when the optimizer assumes independent distributions.

## Evidence notebook

For each run record: database version, session settings, query/plan id, parameter values, rows returned, rows read, estimated rows, elapsed/CPU time, logical/physical IO, spill/temp bytes, memory grant/workarea, lock/wait time, index/table size, concurrent sessions and DML throughput.

The SQL files intentionally do not contain fake timing numbers. Runtime evidence belongs to the environment where you execute the lab.
