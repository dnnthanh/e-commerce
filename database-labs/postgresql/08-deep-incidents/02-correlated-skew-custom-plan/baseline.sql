CREATE INDEX IF NOT EXISTS ix_lab_order_seller_status ON lab_order_header(seller_id,status,created_at DESC);
ANALYZE lab_order_header;
PREPARE order_probe(bigint,text) AS SELECT id,created_at FROM lab_order_header WHERE seller_id=$1 AND status=$2 ORDER BY created_at DESC LIMIT 500;
SET plan_cache_mode=force_generic_plan;
EXPLAIN (ANALYZE,BUFFERS) EXECUTE order_probe(999,'PENDING');
EXPLAIN (ANALYZE,BUFFERS) EXECUTE order_probe(42,'PENDING');
SET plan_cache_mode=force_custom_plan;
EXPLAIN (ANALYZE,BUFFERS) EXECUTE order_probe(999,'PENDING');
EXPLAIN (ANALYZE,BUFFERS) EXECUTE order_probe(42,'PENDING');
