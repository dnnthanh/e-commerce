EXPLAIN SELECT COUNT(*) FROM lab_order_header WHERE DATE(created_at)=CURRENT_DATE;
-- If using a partitioned shadow table, inspect the `partitions` column; function-wrapped partition keys often defeat pruning.
-- Evidence checklist:
-- Run EXPLAIN ANALYZE and EXPLAIN FORMAT=JSON, then capture performance_schema statement/lock rows.
-- Record rows examined vs returned, Handler_read counters, Created_tmp_disk_tables, Sort_merge_passes and InnoDB transaction/lock state.
-- Repeat for hot and long-tail parameters plus concurrent writers; compare index size and INSERT/UPDATE throughput before accepting the change.
