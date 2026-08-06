# SQL Server 2022 deep production incidents

These labs are DBA/troubleshooting exercises. A solution is accepted only after evidence from the baseline and a regression matrix.

## Cases
- [Parameter sniffing, skew and Parameter Sensitive Plan optimization](01-parameter-sniffing-psp/README.md)
- [Key Lookup tipping point and covering-index budget](02-key-lookup-tipping/README.md)
- [Memory grants, sort/hash spill and feedback](03-memory-grant-spill/README.md)
- [Snapshot isolation version store and tempdb pressure](04-tempdb-version-store/README.md)
- [Deadlock graph, lock escalation and access ordering](05-deadlock-lock-escalation/README.md)
- [Partition elimination and aligned indexes](06-partition-elimination/README.md)
- [Query Store plan regression and controlled forcing](07-query-store-regression/README.md)
- [Rowstore/columnstore hybrid for operational analytics](08-columnstore-hybrid/README.md)
