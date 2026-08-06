SET search_path = perf_lab, public;

-- INCIDENT: multiple publishers serialize on the same oldest PENDING rows.
EXPLAIN (ANALYZE, BUFFERS)
SELECT id,event_id,aggregate_id,event_type,payload
FROM outbox_event
WHERE status='PENDING'
ORDER BY created_at,id
LIMIT 200;

CREATE INDEX IF NOT EXISTS lab_outbox_pending_created
ON outbox_event(created_at,id)
INCLUDE(event_id,aggregate_id,event_type)
WHERE status='PENDING';

-- Run this transaction concurrently in 2-8 sessions.
BEGIN;
WITH claim AS MATERIALIZED (
  SELECT id
  FROM outbox_event
  WHERE status='PENDING'
  ORDER BY created_at,id
  FOR UPDATE SKIP LOCKED
  LIMIT 200
)
UPDATE outbox_event o
SET status='PUBLISHING'
FROM claim c
WHERE o.id=c.id
RETURNING o.id,o.event_id,o.aggregate_id,o.event_type;
-- ROLLBACK during lab so batches can be reclaimed.
ROLLBACK;

-- Validate: concurrent sessions claim disjoint ids without waiting.
-- Failure mode still remaining: crash after broker send before DB PROCESSED -> duplicate delivery.
-- Required production control: idempotent consumer/inbox; SKIP LOCKED is not exactly-once.
