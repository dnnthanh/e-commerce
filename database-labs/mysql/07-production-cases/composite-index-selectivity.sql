EXPLAIN ANALYZE
SELECT id,product_id,user_id,rating,status,created_at
FROM product_review
WHERE product_id=100001 AND status='PUBLISHED'
ORDER BY created_at DESC,id DESC
LIMIT 100;
-- Compare separate indexes(product_id), (status), (created_at) against one access-pattern index in isolated runs.
-- Record rows examined and DML cost; low-selectivity status alone is usually weak.
