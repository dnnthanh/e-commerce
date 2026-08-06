-- Mark claimed rows before commit; process network IO outside the claim transaction.
UPDATE lab_outbox SET status='PROCESSING' WHERE id IN (SELECT id FROM lab_outbox WHERE status='PENDING' ORDER BY created_at,id FETCH FIRST 100 ROWS ONLY FOR UPDATE SKIP LOCKED);
COMMIT;
-- Add retry/reaper semantics for PROCESSING rows older than the worker lease.
