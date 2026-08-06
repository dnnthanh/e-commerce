SELECT datname,temp_files,temp_bytes FROM pg_stat_database WHERE datname=current_database();
-- Load test concurrency 1/10/30. Compute memory upper bound = concurrent queries × spill-prone nodes × workers × work_mem; reject settings that threaten host memory.
-- Evidence checklist:
-- 1) save EXPLAIN (ANALYZE,BUFFERS,WAL,SETTINGS) output; 2) compare hot/tail parameters;
-- 3) query pg_stat_activity/pg_locks during concurrency; 4) record relation/index size and pg_stat_user_indexes usage;
-- 5) repeat after ANALYZE and under expected concurrent sessions. Do not accept a latency win without write/storage measurements.
