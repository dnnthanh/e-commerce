# Deep DBA incident matrix

This matrix is generated from the implemented incident packs. Every incident has `README.md`, `baseline.sql`, `solutions.sql`, and `regression.sql`; the README defines evidence, trade-offs, acceptance criteria, and a production decision.

| Engine | Incident | Production focus |
| --- | --- | --- |
| postgresql | `01-join-fanout-cardinality` — Join fan-out, CTE materialization and wrong cardinality | join fan-out / CTE / cardinality |
| postgresql | `02-correlated-skew-custom-plan` — Correlated seller/status skew and generic plan regression | skew / prepared custom-vs-generic plans |
| postgresql | `03-outbox-skip-locked-hot-partition` — High-concurrency outbox claiming with SKIP LOCKED | SKIP LOCKED outbox workers |
| postgresql | `04-mvcc-bloat-visibility` — Long transaction, MVCC bloat and visibility-map loss | MVCC / bloat / visibility map |
| postgresql | `05-sort-hash-spill-workmem` — Sort/hash spill, work_mem and parallel plan trade-offs | sort/hash spill and work_mem |
| postgresql | `06-keyset-concurrent-pagination` — Concurrent pagination without duplicate/missing rows | concurrent keyset pagination |
| postgresql | `07-jsonb-gin-gist-operator-class` — JSONB GIN operator classes and GiST range overlap | JSONB GIN and range GiST |
| postgresql | `08-partition-pruning-index-budget` — Partition pruning, BRIN/B-tree choice and index write budget | partition pruning / BRIN / index budget |
| mysql | `01-leftmost-prefix-skew` — Composite indexes, leftmost prefix and hot-seller skew | leftmost-prefix / histogram / skew |
| mysql | `02-covering-wide-row` — Covering index versus wide/LOB row fetch | covering index vs wide clustered row |
| mysql | `03-filesort-temp-spill` — Filesort, temporary tables and memory limits | filesort / internal temp spill |
| mysql | `04-next-key-deadlock` — Next-key locks, gap locks and deadlock ordering | next-key/gap-lock deadlock |
| mysql | `05-keyset-concurrency` — Deep pagination and concurrent inserts | concurrent keyset pagination |
| mysql | `06-partition-pruning` — Range partition pruning and local maintenance | partition pruning |
| mysql | `07-json-generated-index` — JSON generated columns and selective indexing | JSON generated-column indexing |
| mysql | `08-undo-purge-long-transaction` — Undo history, purge lag and long transactions | undo history / purge lag |
| sqlserver | `01-parameter-sniffing-psp` — Parameter sniffing, skew and Parameter Sensitive Plan optimization | parameter sniffing / PSP |
| sqlserver | `02-key-lookup-tipping` — Key Lookup tipping point and covering-index budget | Key Lookup tipping point |
| sqlserver | `03-memory-grant-spill` — Memory grants, sort/hash spill and feedback | memory grant / spill / feedback |
| sqlserver | `04-tempdb-version-store` — Snapshot isolation version store and tempdb pressure | tempdb version store |
| sqlserver | `05-deadlock-lock-escalation` — Deadlock graph, lock escalation and access ordering | deadlock / lock escalation |
| sqlserver | `06-partition-elimination` — Partition elimination and aligned indexes | partition elimination / aligned indexes |
| sqlserver | `07-query-store-regression` — Query Store plan regression and controlled forcing | Query Store plan regression |
| sqlserver | `08-columnstore-hybrid` — Rowstore/columnstore hybrid for operational analytics | rowstore + columnstore hybrid |
| oracle | `01-bind-peeking-acs` — Bind peeking, skew and adaptive cursor sharing | bind peeking / adaptive cursor sharing |
| oracle | `02-clustering-factor-rowid` — Clustering factor and TABLE ACCESS BY INDEX ROWID cost | clustering factor / rowid cost |
| oracle | `03-pga-temp-spill` — PGA workarea sizing and TEMP spill | PGA workarea / TEMP spill |
| oracle | `04-skip-locked-queue` — SKIP LOCKED work queue fairness and hot blocks | SKIP LOCKED worker queue |
| oracle | `05-partition-local-global-index` — Partition pruning plus local/global index maintenance | local/global partition indexes |
| oracle | `06-undo-snapshot-too-old` — UNDO retention and ORA-01555 snapshot too old | UNDO / snapshot too old |
| oracle | `07-materialized-view-rewrite` — Materialized view refresh and query rewrite | materialized-view rewrite |
| oracle | `08-plan-baseline-invisible-index` — SQL plan management and invisible-index rollout | invisible index / SQL plan baseline |

## Evidence standard

A case is not complete after creating an index or making one query faster. Capture actual plan evidence, hot and long-tail parameters, concurrency where relevant, read/write/storage cost, regression after statistics refresh, rollback, and the production monitoring signal that would invalidate the decision.
