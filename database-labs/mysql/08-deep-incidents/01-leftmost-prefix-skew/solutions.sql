CREATE INDEX ix_order_seller_status_created ON lab_order_header(seller_id,status,created_at DESC,id DESC);
ANALYZE TABLE lab_order_header UPDATE HISTOGRAM ON seller_id,status WITH 128 BUCKETS;
EXPLAIN ANALYZE SELECT id,created_at,status FROM lab_order_header WHERE seller_id=999 AND status='PENDING' AND created_at>=NOW()-INTERVAL 90 DAY ORDER BY created_at DESC LIMIT 500;
-- Compare alternate index (seller_id,created_at,status) against real predicate selectivity; leftmost order is workload-dependent.
