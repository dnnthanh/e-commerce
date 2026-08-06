-- Run in two psql sessions to reproduce inconsistent lock ordering.
-- Session A:
-- BEGIN;
-- SELECT * FROM inventory_balance WHERE sku_id=100 AND warehouse_id=1 FOR UPDATE;
-- SELECT * FROM inventory_balance WHERE sku_id=200 AND warehouse_id=1 FOR UPDATE;

-- Session B (reverse order -> deadlock risk):
-- BEGIN;
-- SELECT * FROM inventory_balance WHERE sku_id=200 AND warehouse_id=1 FOR UPDATE;
-- SELECT * FROM inventory_balance WHERE sku_id=100 AND warehouse_id=1 FOR UPDATE;

-- Production rule: sort lock targets by a deterministic key before acquiring row locks.
-- Keep network/external provider calls OUTSIDE the DB transaction.

SELECT pid,wait_event_type,wait_event,state,xact_start,query_start,left(query,180) query
FROM pg_stat_activity
WHERE datname=current_database() AND state<>'idle'
ORDER BY xact_start NULLS LAST;

SELECT blocked.pid blocked_pid, blocker.pid blocker_pid,
       left(blocked.query,120) blocked_query,
       left(blocker.query,120) blocker_query
FROM pg_stat_activity blocked
JOIN pg_stat_activity blocker ON blocker.pid = ANY(pg_blocking_pids(blocked.pid));
