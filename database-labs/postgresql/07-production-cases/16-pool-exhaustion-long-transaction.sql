-- INCIDENT: API times out waiting for Hikari connections although individual SQL statements look acceptable.
-- Diagnose transaction age and wait state, not only slow query duration.
SELECT pid,usename,application_name,state,
       now()-xact_start AS transaction_age,
       now()-query_start AS query_age,
       wait_event_type,wait_event,
       left(query,200) query
FROM pg_stat_activity
WHERE datname=current_database()
ORDER BY xact_start NULLS LAST;

-- Look specifically for `idle in transaction` and transactions waiting on Client/locks for a long time.
SELECT pid,now()-xact_start age,left(query,200)
FROM pg_stat_activity
WHERE state='idle in transaction'
ORDER BY xact_start;

-- Architecture exercise:
-- BAD: @Transactional -> update DB -> call REST/payment provider for 5s -> update DB -> commit.
-- The JDBC connection and locks stay held during external I/O.
-- BETTER: short local transaction -> commit/outbox/state transition -> external call outside DB transaction -> short reconciliation transaction.
-- Measure Hikari active/pending metrics together with pg_stat_activity to prove the boundary issue.
