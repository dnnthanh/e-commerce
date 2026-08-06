# MySQL 8.4 / InnoDB deep production incidents

These labs are DBA/troubleshooting exercises. A solution is accepted only after evidence from the baseline and a regression matrix.

## Cases
- [Composite indexes, leftmost prefix and hot-seller skew](01-leftmost-prefix-skew/README.md)
- [Covering index versus wide/LOB row fetch](02-covering-wide-row/README.md)
- [Filesort, temporary tables and memory limits](03-filesort-temp-spill/README.md)
- [Next-key locks, gap locks and deadlock ordering](04-next-key-deadlock/README.md)
- [Deep pagination and concurrent inserts](05-keyset-concurrency/README.md)
- [Range partition pruning and local maintenance](06-partition-pruning/README.md)
- [JSON generated columns and selective indexing](07-json-generated-index/README.md)
- [Undo history, purge lag and long transactions](08-undo-purge-long-transaction/README.md)
