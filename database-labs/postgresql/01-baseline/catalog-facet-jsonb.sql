-- Database: catalog_db
-- Facet query: intentionally joins a large JSONB attribute table.
EXPLAIN (ANALYZE, BUFFERS, SETTINGS)
WITH filtered_product AS MATERIALIZED (
  SELECT id
  FROM product
  WHERE status='PUBLISHED'
    AND category_id=1
    AND created_at>=now()-interval '180 day'
)
SELECT d.code,
       v.value_json->>'value' AS facet_value,
       count(*) AS product_count
FROM filtered_product p
JOIN product_attribute_value v ON v.product_id=p.id
JOIN product_attribute_definition d ON d.id=v.attribute_definition_id
WHERE v.value_json @> '{"value":"premium"}'::jsonb
   OR d.code='spec'
GROUP BY d.code,v.value_json->>'value'
ORDER BY d.code,product_count DESC;
