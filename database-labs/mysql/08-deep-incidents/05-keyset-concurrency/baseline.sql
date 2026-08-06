CREATE INDEX ix_order_created_id ON lab_order_header(created_at DESC,id DESC);
EXPLAIN ANALYZE SELECT id,created_at,status FROM lab_order_header ORDER BY created_at DESC,id DESC LIMIT 100 OFFSET 500000;
-- Evidence checklist:
-- Run EXPLAIN ANALYZE and EXPLAIN FORMAT=JSON, then capture performance_schema statement/lock rows.
-- Record rows examined vs returned, Handler_read counters, Created_tmp_disk_tables, Sort_merge_passes and InnoDB transaction/lock state.
-- Repeat for hot and long-tail parameters plus concurrent writers; compare index size and INSERT/UPDATE throughput before accepting the change.
