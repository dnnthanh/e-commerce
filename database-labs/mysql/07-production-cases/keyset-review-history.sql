EXPLAIN ANALYZE
SELECT id,product_id,user_id,rating,status,created_at
FROM product_review
WHERE product_id=100001 AND status='PUBLISHED'
ORDER BY created_at DESC,id DESC
LIMIT 50 OFFSET 100000;

-- Candidate cursor query:
EXPLAIN ANALYZE
SELECT id,product_id,user_id,rating,status,created_at
FROM product_review
WHERE product_id=100001
  AND status='PUBLISHED'
  AND (created_at < @last_created_at OR (created_at=@last_created_at AND id<@last_id))
ORDER BY created_at DESC,id DESC
LIMIT 50;

-- Candidate index: (product_id,status,created_at DESC,id DESC). Validate status selectivity and write cost first.
