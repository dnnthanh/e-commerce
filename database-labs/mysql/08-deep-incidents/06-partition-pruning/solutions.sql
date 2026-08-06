EXPLAIN SELECT COUNT(*) FROM lab_order_header WHERE created_at>=CURRENT_DATE AND created_at<CURRENT_DATE+INTERVAL 1 DAY;
-- Create a monthly RANGE COLUMNS(created_at) shadow table and compare partition pruning + DROP PARTITION retention against DELETE batches.
-- Evidence checklist:
-- Run EXPLAIN ANALYZE and EXPLAIN FORMAT=JSON, then capture performance_schema statement/lock rows.
-- Record rows examined vs returned, Handler_read counters, Created_tmp_disk_tables, Sort_merge_passes and InnoDB transaction/lock state.
-- Repeat for hot and long-tail parameters plus concurrent writers; compare index size and INSERT/UPDATE throughput before accepting the change.
