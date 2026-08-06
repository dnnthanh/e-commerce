SELECT indexrelname,pg_size_pretty(pg_relation_size(indexrelid)) FROM pg_stat_user_indexes WHERE relname IN ('lab_order_line','lab_promotion');
-- Compare bitmap rechecks, index size, insert cost, and query latency for selective/nonselective brands and wide time overlaps.
-- Evidence checklist:
-- 1) save EXPLAIN (ANALYZE,BUFFERS,WAL,SETTINGS) output; 2) compare hot/tail parameters;
-- 3) query pg_stat_activity/pg_locks during concurrency; 4) record relation/index size and pg_stat_user_indexes usage;
-- 5) repeat after ANALYZE and under expected concurrent sessions. Do not accept a latency win without write/storage measurements.
