START TRANSACTION WITH CONSISTENT SNAPSHOT;
SELECT COUNT(*) FROM lab_inventory;
-- Keep this session open. In another session update inventory repeatedly.
SELECT trx_id,trx_started,trx_state,trx_rows_locked,trx_query FROM information_schema.innodb_trx ORDER BY trx_started;
SHOW ENGINE INNODB STATUS;
-- Evidence checklist:
-- Run EXPLAIN ANALYZE and EXPLAIN FORMAT=JSON, then capture performance_schema statement/lock rows.
-- Record rows examined vs returned, Handler_read counters, Created_tmp_disk_tables, Sort_merge_passes and InnoDB transaction/lock state.
-- Repeat for hot and long-tail parameters plus concurrent writers; compare index size and INSERT/UPDATE throughput before accepting the change.
