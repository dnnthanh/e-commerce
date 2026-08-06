# Oracle Database Free 23 deep production incidents

These labs are DBA/troubleshooting exercises. A solution is accepted only after evidence from the baseline and a regression matrix.

## Cases
- [Bind peeking, skew and adaptive cursor sharing](01-bind-peeking-acs/README.md)
- [Clustering factor and TABLE ACCESS BY INDEX ROWID cost](02-clustering-factor-rowid/README.md)
- [PGA workarea sizing and TEMP spill](03-pga-temp-spill/README.md)
- [SKIP LOCKED work queue fairness and hot blocks](04-skip-locked-queue/README.md)
- [Partition pruning plus local/global index maintenance](05-partition-local-global-index/README.md)
- [UNDO retention and ORA-01555 snapshot too old](06-undo-snapshot-too-old/README.md)
- [Materialized view refresh and query rewrite](07-materialized-view-rewrite/README.md)
- [SQL plan management and invisible-index rollout](08-plan-baseline-invisible-index/README.md)
