-- Claim + state transition in one short transaction.
WITH claimed AS (
 SELECT id FROM lab_outbox WHERE status='PENDING' ORDER BY created_at,id FOR UPDATE SKIP LOCKED LIMIT 200
)
UPDATE lab_outbox o SET status='PROCESSING' FROM claimed c WHERE o.id=c.id RETURNING o.id,o.aggregate_id;
-- Do external IO after commit; final acknowledgement is a separate idempotent update.
CREATE INDEX CONCURRENTLY IF NOT EXISTS ix_lab_outbox_processing ON lab_outbox(id) WHERE status='PROCESSING';
