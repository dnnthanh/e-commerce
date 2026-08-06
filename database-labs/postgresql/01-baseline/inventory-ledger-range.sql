-- Database: inventory_db
-- Time-series range + SKU filter. Compare B-tree/BRIN and later partition pruning.
EXPLAIN (ANALYZE, BUFFERS, WAL, SETTINGS)
SELECT sku_id,warehouse_id,reason,
       sum(delta) AS net_delta,
       count(*) AS movement_count,
       max(created_at) AS last_movement
FROM inventory_ledger
WHERE created_at>=date_trunc('month',now())-interval '5 month'
  AND created_at<date_trunc('month',now())+interval '1 month'
  AND sku_id BETWEEN 2000001 AND 2050000
GROUP BY sku_id,warehouse_id,reason
HAVING sum(abs(delta))>3
ORDER BY last_movement DESC
LIMIT 500;
