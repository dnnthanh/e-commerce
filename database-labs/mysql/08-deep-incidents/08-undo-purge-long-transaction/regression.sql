SELECT * FROM information_schema.innodb_trx ORDER BY trx_started;
-- Record History list length from SHOW ENGINE INNODB STATUS before/during/after the long transaction and measure write/read latency.
-- Evidence checklist:
-- Run EXPLAIN ANALYZE and EXPLAIN FORMAT=JSON, then capture performance_schema statement/lock rows.
-- Record rows examined vs returned, Handler_read counters, Created_tmp_disk_tables, Sort_merge_passes and InnoDB transaction/lock state.
-- Repeat for hot and long-tail parameters plus concurrent writers; compare index size and INSERT/UPDATE throughput before accepting the change.
