EXPLAIN ANALYZE
SELECT id, product_id, rating, title, created_at
FROM review
WHERE product_id = 100001 AND status = 'PUBLISHED'
ORDER BY created_at DESC
LIMIT 100;
