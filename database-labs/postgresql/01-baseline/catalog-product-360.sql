-- Database: catalog_db
-- Goal: one product-search/read-model query with filtering + LATERAL rollups.
-- First run this WITHOUT optional lab indexes.
EXPLAIN (ANALYZE, BUFFERS, WAL, SETTINGS)
WITH candidate AS MATERIALIZED (
  SELECT p.id,p.seller_id,p.category_id,p.name,p.status,p.created_at
  FROM product p
  WHERE p.status='PUBLISHED'
    AND p.category_id IN (1,2,3)
    AND lower(p.name) LIKE 'nova%'
  ORDER BY p.created_at DESC,p.id DESC
  LIMIT 200
)
SELECT c.*,
       sku_info.active_sku_count,
       sku_info.sku_names,
       attr_info.attributes
FROM candidate c
LEFT JOIN LATERAL (
  SELECT count(*) FILTER (WHERE s.active) AS active_sku_count,
         jsonb_agg(jsonb_build_object('skuId',s.id,'variant',s.variant_name) ORDER BY s.id) AS sku_names
  FROM sku s
  WHERE s.product_id=c.id
) sku_info ON true
LEFT JOIN LATERAL (
  SELECT jsonb_object_agg(d.code,v.value_json) AS attributes
  FROM product_attribute_value v
  JOIN product_attribute_definition d ON d.id=v.attribute_definition_id
  WHERE v.product_id=c.id
) attr_info ON true
ORDER BY c.created_at DESC,c.id DESC;
