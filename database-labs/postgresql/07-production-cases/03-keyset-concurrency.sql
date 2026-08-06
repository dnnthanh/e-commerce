SET search_path = perf_lab, public;

-- INCIDENT A: deep OFFSET cost grows with page depth.
EXPLAIN (ANALYZE, BUFFERS)
SELECT id,payment_key,status,amount,created_at
FROM payment
WHERE user_id=777
ORDER BY created_at DESC,id DESC
LIMIT 50 OFFSET 10000;

-- Candidate keyset access pattern. Capture cursor from the previous page.
-- Replace values with real row values from page N.
EXPLAIN (ANALYZE, BUFFERS)
SELECT id,payment_key,status,amount,created_at
FROM payment
WHERE user_id=777
  AND (created_at,id) < (now() - interval '30 days', 9223372036854775807)
ORDER BY created_at DESC,id DESC
LIMIT 50;

-- Candidate supporting index, apply separately and remeasure.
-- CREATE INDEX CONCURRENTLY lab_payment_user_created_id
--   ON payment(user_id,created_at DESC,id DESC)
--   INCLUDE(payment_key,status,amount,provider);

-- INCIDENT B: ORDER BY created_at only is unstable when timestamps tie and concurrent inserts occur.
-- Demonstration: never use this for pageable production APIs:
SELECT id,created_at FROM payment ORDER BY created_at DESC LIMIT 50 OFFSET 50;
-- Stable order requires the unique tie-breaker: ORDER BY created_at DESC,id DESC.
