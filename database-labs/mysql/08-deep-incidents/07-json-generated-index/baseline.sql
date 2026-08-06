ALTER TABLE lab_order_header ADD COLUMN channel VARCHAR(16) GENERATED ALWAYS AS (JSON_UNQUOTE(JSON_EXTRACT(payload,'$.channel'))) STORED;
EXPLAIN ANALYZE SELECT id FROM lab_order_header WHERE JSON_UNQUOTE(JSON_EXTRACT(payload,'$.channel'))='MOBILE' AND seller_id=999 LIMIT 1000;
-- Evidence checklist:
-- Run EXPLAIN ANALYZE and EXPLAIN FORMAT=JSON, then capture performance_schema statement/lock rows.
-- Record rows examined vs returned, Handler_read counters, Created_tmp_disk_tables, Sort_merge_passes and InnoDB transaction/lock state.
-- Repeat for hot and long-tail parameters plus concurrent writers; compare index size and INSERT/UPDATE throughput before accepting the change.
