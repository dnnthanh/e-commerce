SET search_path = perf_lab, public;

-- INCIDENT: reconciliation checks rare UNKNOWN payments every minute while PAID is ~95% of history.
EXPLAIN (ANALYZE, BUFFERS)
SELECT id,payment_key,user_id,provider,amount,created_at
FROM payment
WHERE status='UNKNOWN'
ORDER BY created_at,id
LIMIT 500;

-- Broad status index is often bigger than needed and receives writes for every status.
-- Preferred experiment: active operational subset only.
CREATE INDEX IF NOT EXISTS lab_payment_unknown_reconcile
ON payment(created_at,id)
INCLUDE(payment_key,user_id,provider,amount)
WHERE status='UNKNOWN';

ANALYZE payment;
EXPLAIN (ANALYZE, BUFFERS)
SELECT id,payment_key,user_id,provider,amount,created_at
FROM payment
WHERE status='UNKNOWN'
ORDER BY created_at,id
LIMIT 500;

SELECT pg_size_pretty(pg_relation_size('lab_payment_unknown_reconcile')) partial_index_size;
-- Regression: measure INSERT/UPDATE payment throughput before/after; confirm predicate exactly matches query.
