CREATE INDEX ix_order_created_seller_status ON lab_order_header(created_at,seller_id,status);
-- Test a bounded session increase only after estimating concurrent connections using the same buffers.
SET SESSION tmp_table_size=67108864,max_heap_table_size=67108864,sort_buffer_size=4194304;
EXPLAIN ANALYZE SELECT seller_id,status,COUNT(*) c FROM lab_order_header WHERE created_at>=NOW()-INTERVAL 365 DAY GROUP BY seller_id,status ORDER BY c DESC LIMIT 5000;
