SET search_path = perf_lab, public;

-- INCIDENT: seller=1 strongly correlates with category=10 and PUBLISHED.
-- Single-column statistics assume more independence than the real data has.
EXPLAIN (ANALYZE, BUFFERS)
SELECT id,name
FROM product
WHERE seller_id=1 AND category_id=10 AND status='PUBLISHED'
ORDER BY created_at DESC
LIMIT 100;

-- Record estimated rows vs actual rows before creating any new index.
CREATE STATISTICS IF NOT EXISTS lab_product_seller_category_status (dependencies,mcv)
ON seller_id,category_id,status FROM product;
ANALYZE product;

EXPLAIN (ANALYZE, BUFFERS)
SELECT id,name
FROM product
WHERE seller_id=1 AND category_id=10 AND status='PUBLISHED'
ORDER BY created_at DESC
LIMIT 100;

-- Compare with a long-tail seller where the correlation is different.
EXPLAIN (ANALYZE, BUFFERS)
SELECT id,name
FROM product
WHERE seller_id=777 AND category_id=10 AND status='PUBLISHED'
ORDER BY created_at DESC
LIMIT 100;

-- Decision question: did a better estimate change join/scan choice enough to solve the incident?
-- If no, only then design an index around the actual predicate + ordering contract.
