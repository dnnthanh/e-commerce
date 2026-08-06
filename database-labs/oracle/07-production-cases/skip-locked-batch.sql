-- Run concurrently from two sessions. Each worker should own a disjoint batch.
SELECT settlement_no
FROM settlement
WHERE status='READY'
ORDER BY period_start,settlement_no
FETCH FIRST 100 ROWS ONLY
FOR UPDATE SKIP LOCKED;
-- Keep transaction short; idempotency/reconciliation is still required if a worker crashes after an external side effect.
