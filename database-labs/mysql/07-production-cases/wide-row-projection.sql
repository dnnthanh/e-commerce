EXPLAIN ANALYZE
SELECT id,status,created_at
FROM product_review
WHERE product_id=100001
ORDER BY created_at DESC,id DESC
LIMIT 200;

EXPLAIN ANALYZE
SELECT *
FROM product_review
WHERE product_id=100001
ORDER BY created_at DESC,id DESC
LIMIT 200;
-- Compare row bytes/network latency when body/moderation payload is large. Prefer list projection + detail endpoint over bloating indexes.
