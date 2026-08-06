CREATE INDEX IF NOT EXISTS ix_lab_order_created_id ON lab_order_header(created_at DESC,id DESC);
EXPLAIN (ANALYZE,BUFFERS) SELECT id,created_at,status FROM lab_order_header ORDER BY created_at DESC,id DESC LIMIT 100 OFFSET 500000;
-- In another session continuously insert rows with current timestamp while paging.
-- Evidence checklist:
-- 1) save EXPLAIN (ANALYZE,BUFFERS,WAL,SETTINGS) output; 2) compare hot/tail parameters;
-- 3) query pg_stat_activity/pg_locks during concurrency; 4) record relation/index size and pg_stat_user_indexes usage;
-- 5) repeat after ANALYZE and under expected concurrent sessions. Do not accept a latency win without write/storage measurements.
