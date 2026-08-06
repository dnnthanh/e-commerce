SET SESSION sort_buffer_size=262144, tmp_table_size=1048576, max_heap_table_size=1048576;
EXPLAIN ANALYZE SELECT seller_id,status,COUNT(*) c FROM lab_order_header WHERE created_at>=NOW()-INTERVAL 365 DAY GROUP BY seller_id,status ORDER BY c DESC LIMIT 5000;
SHOW SESSION STATUS LIKE 'Created_tmp%'; SHOW SESSION STATUS LIKE 'Sort_merge_passes';
