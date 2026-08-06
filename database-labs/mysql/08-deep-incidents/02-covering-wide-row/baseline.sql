EXPLAIN ANALYZE SELECT id,order_id,sku_id FROM lab_order_line WHERE order_id BETWEEN 100000 AND 120000;
EXPLAIN ANALYZE SELECT * FROM lab_order_line WHERE order_id BETWEEN 100000 AND 120000;
SHOW SESSION STATUS LIKE 'Innodb_buffer_pool_reads';
-- Evidence checklist:
-- Run EXPLAIN ANALYZE and EXPLAIN FORMAT=JSON, then capture performance_schema statement/lock rows.
-- Record rows examined vs returned, Handler_read counters, Created_tmp_disk_tables, Sort_merge_passes and InnoDB transaction/lock state.
-- Repeat for hot and long-tail parameters plus concurrent writers; compare index size and INSERT/UPDATE throughput before accepting the change.
