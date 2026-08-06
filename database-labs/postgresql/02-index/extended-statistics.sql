-- Database: catalog_db
-- Single-column statistics can misestimate correlated seller/category/status predicates.
-- Compare estimates before/after CREATE STATISTICS + ANALYZE.
EXPLAIN (ANALYZE,BUFFERS)
SELECT count(*)
FROM product
WHERE seller_id BETWEEN 10001 AND 10020
  AND category_id IN (1,2,3)
  AND status='PUBLISHED';

CREATE STATISTICS st_product_seller_category_status (dependencies, mcv)
ON seller_id,category_id,status
FROM product;
ANALYZE product;

EXPLAIN (ANALYZE,BUFFERS)
SELECT count(*)
FROM product
WHERE seller_id BETWEEN 10001 AND 10020
  AND category_id IN (1,2,3)
  AND status='PUBLISHED';
