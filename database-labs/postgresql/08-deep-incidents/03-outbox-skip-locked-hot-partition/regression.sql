SELECT status,count(*) FROM lab_outbox GROUP BY status;
SELECT n_dead_tup,last_autovacuum FROM pg_stat_user_tables WHERE relname='lab_outbox';
-- Run 1/5/30 workers for 60s. Record events/s, claim p95, duplicates, lock waits, WAL bytes and starvation age of oldest PENDING row.
-- Evidence checklist:
-- 1) save EXPLAIN (ANALYZE,BUFFERS,WAL,SETTINGS) output; 2) compare hot/tail parameters;
-- 3) query pg_stat_activity/pg_locks during concurrency; 4) record relation/index size and pg_stat_user_indexes usage;
-- 5) repeat after ANALYZE and under expected concurrent sessions. Do not accept a latency win without write/storage measurements.
