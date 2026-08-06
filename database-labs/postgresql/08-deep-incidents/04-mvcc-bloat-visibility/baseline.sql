-- Session A: create a deliberately old snapshot and keep it open.
BEGIN ISOLATION LEVEL REPEATABLE READ;
SELECT count(*) FROM lab_inventory;
SELECT pg_backend_pid();
-- Session B: repeatedly update rows and run VACUUM in another terminal.
-- UPDATE lab_inventory SET reserved=(reserved+1)%20,version=version+1,updated_at=now() WHERE sku_id<=200000;
-- VACUUM (VERBOSE,ANALYZE) lab_inventory;
SELECT pid,backend_xmin,xact_start,state,query FROM pg_stat_activity WHERE backend_xmin IS NOT NULL ORDER BY xact_start;
SELECT n_live_tup,n_dead_tup,last_vacuum,last_autovacuum FROM pg_stat_user_tables WHERE relname='lab_inventory';
