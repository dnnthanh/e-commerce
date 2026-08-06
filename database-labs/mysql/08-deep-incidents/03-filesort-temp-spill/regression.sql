SHOW SESSION STATUS LIKE 'Created_tmp_disk_tables'; SHOW SESSION STATUS LIKE 'Sort_merge_passes';
-- Run 1/20/100 concurrent sessions; reject memory settings that trade spill reduction for host OOM risk.
-- Evidence checklist:
-- Run EXPLAIN ANALYZE and EXPLAIN FORMAT=JSON, then capture performance_schema statement/lock rows.
-- Record rows examined vs returned, Handler_read counters, Created_tmp_disk_tables, Sort_merge_passes and InnoDB transaction/lock state.
-- Repeat for hot and long-tail parameters plus concurrent writers; compare index size and INSERT/UPDATE throughput before accepting the change.
