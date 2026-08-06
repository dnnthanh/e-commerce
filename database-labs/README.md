# Database Performance & Production Incident Labs

Bộ lab này mô phỏng công việc DBA/performance engineer trên hệ thống marketplace nhiều DB engine. Mục tiêu không phải nhớ cú pháp index mà là đi hết vòng đời incident:

`SLO/symptom -> data shape -> baseline plan -> evidence -> root cause -> competing fixes -> trade-off -> regression -> production decision`.

## Maturity levels

1. `01-baseline`: query gốc và data access path chưa tối ưu.
2. `02-index`: index mechanics riêng lẻ để học operator/index behavior.
3. `03-partition`: pruning, retention, local/global/aligned indexes.
4. `04-plan-analysis`: execution-plan vocabulary và evidence capture.
5. `05-advanced`: concurrency/window/lateral/engine-specific SQL.
6. `06-case-studies`: bounded-context reconciliation/report workflows.
7. `07-production-cases`: incident vừa phải, một file tập trung một vấn đề.
8. `08-deep-incidents`: **lab cấp production**. Mỗi incident có `README.md`, `baseline.sql`, `solutions.sql`, `regression.sql`; dùng skew/millions-of-rows/concurrent writers và bắt buộc production decision.

## Deep incidents by engine

### PostgreSQL 17

8 incident packs: CTE/join fan-out, correlated skew/generic plan, outbox `SKIP LOCKED`, MVCC/bloat/visibility-map, sort/hash spill + `work_mem`, keyset under concurrent insert, JSONB GIN + GiST overlap, partition pruning/BRIN/index-write budget.

Evidence focus: `EXPLAIN (ANALYZE, BUFFERS, WAL, SETTINGS)`, estimate vs actual, loops, Rows Removed, temp spill, heap fetches, `pg_stat_statements`, `pg_stat_activity`, `pg_locks`, visibility/dead tuples, index usage/size.

### MySQL 8.4 / InnoDB

8 incident packs: leftmost-prefix + skew, covering vs wide LOB rows, filesort/temp spill, next-key/gap-lock deadlock, keyset pagination, partition pruning, JSON generated-column indexing, undo/purge lag from long transactions.

Evidence focus: `EXPLAIN ANALYZE`, `EXPLAIN FORMAT=JSON`, `performance_schema`, `sys`, handler reads, `Created_tmp_disk_tables`, `Sort_merge_passes`, `data_locks`, `INNODB_TRX`, purge/history-list behavior.

### SQL Server 2022

8 incident packs: parameter sniffing/PSP, Key Lookup tipping point, memory grant spill/feedback, tempdb version store, deadlock + lock escalation, partition elimination/aligned index, Query Store regression/controlled forcing, rowstore + nonclustered columnstore hybrid.

Evidence focus: Actual Execution Plan, `SET STATISTICS IO,TIME`, Query Store, Extended Events deadlock graph, `sys.dm_exec_query_stats`, memory grants, tempdb/version-store DMVs, columnstore rowgroup health.

### Oracle 23

8 incident packs: bind peeking/adaptive cursor sharing, clustering factor + rowid access, PGA/TEMP spill, `SKIP LOCKED` queue, partition local/global index maintenance, UNDO/ORA-01555, materialized-view query rewrite, invisible-index + SQL plan management rollout.

Evidence focus: `DBMS_XPLAN.DISPLAY_CURSOR('ALLSTATS LAST +PEEKED_BINDS +PARTITION +IOSTATS')`, `V$SQL`, workarea/PGA views, `V$SESSION`, `V$UNDOSTAT`, partition/index metadata and cursor child-plan behavior.

## Rules for every deep incident

- Keep the skew. Uniform random data is not a realistic optimizer test.
- Capture hot-key and long-tail plans before any fix.
- Change one variable per experiment.
- Do not add an index without measuring index size and write cost.
- Do not raise memory globally from one slow query without a concurrency budget.
- Do not use hints/plan forcing as the first response; treat them as controlled mitigations with rollback.
- Concurrency incidents require at least two sessions/workers and wait/lock evidence.
- Pagination tests must run while inserts occur.
- Partition tests must prove pruning/partition elimination in the actual plan.
- A lab is incomplete until `regression.sql` is run and the notes end with `ACCEPT`, `REJECT`, or `TEMPORARY MITIGATION`.

See [`LEARNING-GUIDE.md`](./LEARNING-GUIDE.md) and [`INCIDENT-TEMPLATE.md`](./INCIDENT-TEMPLATE.md).

See [`DEEP-INCIDENT-MATRIX.md`](DEEP-INCIDENT-MATRIX.md) for the implemented cross-engine incident inventory.
