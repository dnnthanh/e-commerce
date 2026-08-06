-- Fix transaction scope first: paginate/stream reports with bounded transactions or use replicas where appropriate.
-- Verify purge catches up after old transaction ends; do not rely on increasing undo retention as the only fix.
COMMIT;
-- Evidence checklist:
-- Run EXPLAIN ANALYZE and EXPLAIN FORMAT=JSON, then capture performance_schema statement/lock rows.
-- Record rows examined vs returned, Handler_read counters, Created_tmp_disk_tables, Sort_merge_passes and InnoDB transaction/lock state.
-- Repeat for hot and long-tail parameters plus concurrent writers; compare index size and INSERT/UPDATE throughput before accepting the change.
