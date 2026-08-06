-- Database: review_db
-- Partition a LAB COPY, because MySQL requires every UNIQUE/PRIMARY key to contain the partition key.
DROP TABLE IF EXISTS review_history_lab;
CREATE TABLE review_history_lab (
  id BIGINT NOT NULL,
  user_id VARCHAR(64) NOT NULL,
  product_id BIGINT NOT NULL,
  order_line_id BIGINT NOT NULL,
  rating INT NOT NULL,
  title VARCHAR(255),
  status VARCHAR(32) NOT NULL,
  created_at DATETIME NOT NULL,
  KEY idx_review_history_product_created(product_id,created_at DESC)
)
PARTITION BY RANGE COLUMNS(created_at) (
  PARTITION p2025q1 VALUES LESS THAN ('2025-04-01'),
  PARTITION p2025q2 VALUES LESS THAN ('2025-07-01'),
  PARTITION p2025q3 VALUES LESS THAN ('2025-10-01'),
  PARTITION p2025q4 VALUES LESS THAN ('2026-01-01'),
  PARTITION p2026q1 VALUES LESS THAN ('2026-04-01'),
  PARTITION p2026q2 VALUES LESS THAN ('2026-07-01'),
  PARTITION p2026q3 VALUES LESS THAN ('2026-10-01'),
  PARTITION pmax VALUES LESS THAN (MAXVALUE)
);

INSERT INTO review_history_lab(id,user_id,product_id,order_line_id,rating,title,status,created_at)
SELECT id,user_id,product_id,order_line_id,rating,title,status,created_at FROM review;

EXPLAIN ANALYZE
SELECT product_id,rating,count(*)
FROM review_history_lab
WHERE created_at>='2026-07-01' AND created_at<'2026-10-01'
  AND product_id BETWEEN 1000001 AND 1050000
GROUP BY product_id,rating;
