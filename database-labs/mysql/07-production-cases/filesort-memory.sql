EXPLAIN ANALYZE
SELECT product_id,user_id,rating,created_at
FROM product_review
WHERE status='PUBLISHED'
ORDER BY rating DESC,created_at DESC
LIMIT 5000;
-- Look for sort behavior and rows examined. A specialized index may help only if this ordering is a real high-frequency contract; otherwise search/read-model may be better.
