-- Database: inventory_db
-- Combines filtered aggregates, window ranking and LATERAL latest-row lookup.
EXPLAIN (ANALYZE,BUFFERS,SETTINGS)
WITH movement AS (
  SELECT sku_id,warehouse_id,
    sum(-delta) FILTER (WHERE delta<0 AND reason='ORDER_CONFIRMED') AS sold_units,
    sum(delta) FILTER (WHERE delta>0 AND reason='RETURN_ACCEPTED') AS returned_units,
    count(*) AS movement_count
  FROM inventory_ledger
  WHERE created_at>=now()-interval '90 day'
  GROUP BY sku_id,warehouse_id
), ranked AS (
  SELECT m.*,
    row_number() OVER(PARTITION BY warehouse_id ORDER BY sold_units DESC NULLS LAST,sku_id) AS sales_rank
  FROM movement m
)
SELECT r.sku_id,r.warehouse_id,r.sold_units,r.returned_units,r.sales_rank,
       latest.reason AS latest_reason,latest.delta AS latest_delta,latest.created_at AS latest_at
FROM ranked r
LEFT JOIN LATERAL (
  SELECT l.reason,l.delta,l.created_at
  FROM inventory_ledger l
  WHERE l.sku_id=r.sku_id AND l.warehouse_id=r.warehouse_id
  ORDER BY l.created_at DESC,l.id DESC
  LIMIT 1
) latest ON true
WHERE r.sales_rank<=20
ORDER BY r.warehouse_id,r.sales_rank;
