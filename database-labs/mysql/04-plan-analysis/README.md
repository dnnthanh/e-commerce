# MySQL EXPLAIN ANALYZE checklist

Inspect actual loop counts/time, access type, selected key, rows examined, filtering, filesort and whether the composite index satisfies both filtering and ordering. Test left-prefix behavior by changing predicate order/columns rather than memorizing an index rule.
