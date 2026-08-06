SELECT seller_id,status,COUNT(*) c FROM lab_order_header GROUP BY seller_id,status ORDER BY c DESC LIMIT 30;
-- Compare rows examined and p95 for seller 999 and long-tail sellers across PENDING/COMPLETED statuses.
-- Evidence checklist:
-- Run EXPLAIN ANALYZE and EXPLAIN FORMAT=JSON, then capture performance_schema statement/lock rows.
-- Record rows examined vs returned, Handler_read counters, Created_tmp_disk_tables, Sort_merge_passes and InnoDB transaction/lock state.
-- Repeat for hot and long-tail parameters plus concurrent writers; compare index size and INSERT/UPDATE throughput before accepting the change.
