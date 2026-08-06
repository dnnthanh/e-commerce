-- Database: payment_db
-- Compare deep OFFSET with keyset pagination. Use the same page size and ordering.

-- OFFSET: database must discover/sort/skip all earlier rows.
EXPLAIN (ANALYZE, BUFFERS, SETTINGS)
SELECT id,payment_key,user_id,amount,status,created_at
FROM payment
WHERE user_id BETWEEN 'customer-0000001' AND 'customer-0250000'
ORDER BY created_at DESC,id DESC
LIMIT 50 OFFSET 200000;

-- keyset: replace the literals with the last (created_at,id) from the previous page.
-- This is the cursor predicate: (created_at,id) < (:last_created_at,:last_id).
EXPLAIN (ANALYZE, BUFFERS, SETTINGS)
SELECT id,payment_key,user_id,amount,status,created_at
FROM payment
WHERE user_id BETWEEN 'customer-0000001' AND 'customer-0250000'
  AND (created_at,id)<(now()-interval '30 day',900000)
ORDER BY created_at DESC,id DESC
LIMIT 50;
