-- Database: catalog_db (same pattern applies to other PostgreSQL outboxes).
-- Multi-instance queue claim: each worker atomically claims a disjoint batch.
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
RETURNING o.id,o.event_id,o.aggregate_id,o.event_type,o.created_at;
COMMIT;

-- Learning exercise: open two psql sessions, execute BEGIN + claim in both before COMMIT,
-- and confirm the returned id sets do not overlap.
