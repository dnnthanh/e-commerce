EXPLAIN ANALYZE SELECT id,created_at,status FROM lab_order_header WHERE seller_id=999 AND status='PENDING' AND created_at>=NOW()-INTERVAL 90 DAY ORDER BY created_at DESC LIMIT 500;
EXPLAIN FORMAT=JSON SELECT id,created_at,status FROM lab_order_header WHERE seller_id=42 AND status='PENDING' AND created_at>=NOW()-INTERVAL 90 DAY ORDER BY created_at DESC LIMIT 500;
SHOW SESSION STATUS LIKE 'Handler_read%';
