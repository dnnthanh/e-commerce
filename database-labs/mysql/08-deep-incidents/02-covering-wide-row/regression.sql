SHOW TABLE STATUS LIKE 'lab_order_line';
SHOW INDEX FROM lab_order_line;
-- Compare buffer-pool reads and bytes returned for projection vs SELECT * under cold/warm cache.
-- Evidence checklist:
-- Run EXPLAIN ANALYZE and EXPLAIN FORMAT=JSON, then capture performance_schema statement/lock rows.
-- Record rows examined vs returned, Handler_read counters, Created_tmp_disk_tables, Sort_merge_passes and InnoDB transaction/lock state.
-- Repeat for hot and long-tail parameters plus concurrent writers; compare index size and INSERT/UPDATE throughput before accepting the change.
