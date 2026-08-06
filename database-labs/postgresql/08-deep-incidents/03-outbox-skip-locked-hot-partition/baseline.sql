CREATE INDEX IF NOT EXISTS ix_lab_outbox_pending_created ON lab_outbox(created_at,id) WHERE status='PENDING';
BEGIN;
EXPLAIN (ANALYZE,BUFFERS,WAL)
SELECT id,aggregate_id FROM lab_outbox WHERE status='PENDING' ORDER BY created_at,id FOR UPDATE SKIP LOCKED LIMIT 200;
-- Keep transaction open in session A. Run the same statement in sessions B/C and verify disjoint id sets.
SELECT pid,wait_event_type,wait_event,query FROM pg_stat_activity WHERE datname=current_database();
ROLLBACK;
