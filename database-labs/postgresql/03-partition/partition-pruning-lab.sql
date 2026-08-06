-- Database: inventory_db
SET enable_partition_pruning=on;

-- GOOD: direct predicate on the partition key lets the planner prune children.
EXPLAIN (ANALYZE,BUFFERS,SETTINGS)
SELECT sku_id,warehouse_id,sum(delta)
FROM inventory_ledger_lab
WHERE created_at>=date_trunc('month',now())-interval '2 month'
  AND created_at<date_trunc('month',now())+interval '1 month'
GROUP BY sku_id,warehouse_id;

-- ANTI-PATTERN: wrapping the partition key can prevent effective pruning/index usage.
EXPLAIN (ANALYZE,BUFFERS,SETTINGS)
SELECT sku_id,warehouse_id,sum(delta)
FROM inventory_ledger_lab
WHERE date(created_at)>=current_date-60
GROUP BY sku_id,warehouse_id;
