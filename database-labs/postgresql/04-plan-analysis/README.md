# PostgreSQL plan reading checklist

For the Outbox query inspect: `Seq Scan` vs `Index Scan`, rows removed by filter, sort method, shared hit/read blocks, temp read/write, planning/execution time, and whether `LIMIT` can stop early. With millions of historical `PROCESSED` rows and a small active `PENDING` set, a partial index is often smaller than `(status, created_at)`, but the actual plan is the evidence.

Do not conclude partitioning is faster merely because partitions exist. Check `Partition Pruning`, number of partitions scanned, and whether the query predicate contains the partition key.
