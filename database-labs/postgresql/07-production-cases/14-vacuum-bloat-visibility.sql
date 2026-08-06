-- INCIDENT: previously fast index-only query starts heap-fetching heavily after update churn.
SELECT relname,n_live_tup,n_dead_tup,last_autovacuum,last_autoanalyze,autovacuum_count,autoanalyze_count
FROM pg_stat_user_tables
ORDER BY n_dead_tup DESC LIMIT 20;

SELECT relname,idx_scan,idx_tup_read,idx_tup_fetch
FROM pg_stat_user_indexes
ORDER BY idx_tup_fetch DESC LIMIT 30;

-- PostgreSQL contrib pgstattuple is useful in a lab when installed:
-- CREATE EXTENSION IF NOT EXISTS pgstattuple;
-- SELECT * FROM pgstattuple('perf_lab.payment');

-- Compare EXPLAIN (ANALYZE,BUFFERS) before/after VACUUM (ANALYZE) on the lab only.
-- Production decision tree:
-- * autovacuum too slow -> table-specific thresholds/cost settings;
-- * HOT updates prevented by indexed changing columns -> reconsider indexes/fillfactor;
-- * index severely bloated -> REINDEX CONCURRENTLY after evidence;
-- * do not schedule VACUUM FULL casually: it takes an ACCESS EXCLUSIVE lock and rewrites the table.
