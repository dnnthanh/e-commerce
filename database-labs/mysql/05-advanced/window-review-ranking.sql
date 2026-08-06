-- Database: review_db
-- Window functions on a realistic rating distribution.
EXPLAIN ANALYZE
WITH product_stats AS (
  SELECT product_id,
         count(*) AS review_count,
         avg(rating) AS avg_rating,
         sum(rating=5) AS five_star_count,
         sum(rating<=2) AS low_rating_count
  FROM review
  WHERE status='PUBLISHED' AND created_at>=NOW()-INTERVAL 180 DAY
  GROUP BY product_id
), ranked AS (
  SELECT *,
    ROW_NUMBER() OVER(ORDER BY review_count DESC,product_id) AS volume_rank,
    DENSE_RANK() OVER(ORDER BY avg_rating DESC) AS rating_rank
  FROM product_stats
  WHERE review_count>=2
)
SELECT * FROM ranked
WHERE volume_rank<=100 OR rating_rank<=20
ORDER BY volume_rank;
