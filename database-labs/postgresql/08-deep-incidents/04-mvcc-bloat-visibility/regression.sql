SELECT pg_size_pretty(pg_total_relation_size('lab_inventory')) total_size,n_dead_tup,last_autovacuum FROM pg_stat_user_tables WHERE relname='lab_inventory';
-- Track Heap Fetches in Index Only Scan before and after old snapshot ends + VACUUM. Confirm dead tuples decline without blocking OLTP.
-- Evidence checklist:
-- 1) save EXPLAIN (ANALYZE,BUFFERS,WAL,SETTINGS) output; 2) compare hot/tail parameters;
-- 3) query pg_stat_activity/pg_locks during concurrency; 4) record relation/index size and pg_stat_user_indexes usage;
-- 5) repeat after ANALYZE and under expected concurrent sessions. Do not accept a latency win without write/storage measurements.
