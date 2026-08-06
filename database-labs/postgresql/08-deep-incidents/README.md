# PostgreSQL 17 deep production incidents

These labs are DBA/troubleshooting exercises. A solution is accepted only after evidence from the baseline and a regression matrix.

## Cases
- [Join fan-out, CTE materialization and wrong cardinality](01-join-fanout-cardinality/README.md)
- [Correlated seller/status skew and generic plan regression](02-correlated-skew-custom-plan/README.md)
- [High-concurrency outbox claiming with SKIP LOCKED](03-outbox-skip-locked-hot-partition/README.md)
- [Long transaction, MVCC bloat and visibility-map loss](04-mvcc-bloat-visibility/README.md)
- [Sort/hash spill, work_mem and parallel plan trade-offs](05-sort-hash-spill-workmem/README.md)
- [Concurrent pagination without duplicate/missing rows](06-keyset-concurrent-pagination/README.md)
- [JSONB GIN operator classes and GiST range overlap](07-jsonb-gin-gist-operator-class/README.md)
- [Partition pruning, BRIN/B-tree choice and index write budget](08-partition-pruning-index-budget/README.md)
