-- Reconcile current balance against ledger and active reservations.
-- This intentionally combines CTE aggregates + FILTER + FULL OUTER JOIN so missing dimensions are visible.
EXPLAIN (ANALYZE, BUFFERS, WAL, SETTINGS)
WITH ledger AS MATERIALIZED (
  SELECT sku_id,warehouse_id,
         sum(delta) AS ledger_on_hand,
         max(created_at) AS last_movement_at
  FROM inventory_ledger
  WHERE created_at >= now()-interval '180 days'
  GROUP BY sku_id,warehouse_id
), active_reservation AS MATERIALIZED (
  SELECT sku_id,warehouse_id,
         sum(quantity) FILTER (WHERE status='RESERVED' AND expires_at>now()) AS active_reserved,
         count(*) FILTER (WHERE status='RESERVED' AND expires_at<=now()) AS expired_not_released
  FROM inventory_reservation
  GROUP BY sku_id,warehouse_id
), reconciled AS (
  SELECT coalesce(b.sku_id,l.sku_id,r.sku_id) AS sku_id,
         coalesce(b.warehouse_id,l.warehouse_id,r.warehouse_id) AS warehouse_id,
         b.on_hand,b.reserved,l.ledger_on_hand,coalesce(r.active_reserved,0) AS active_reserved,
         coalesce(r.expired_not_released,0) AS expired_not_released,l.last_movement_at
  FROM inventory_balance b
  FULL OUTER JOIN ledger l USING(sku_id,warehouse_id)
  FULL OUTER JOIN active_reservation r
    ON r.sku_id=coalesce(b.sku_id,l.sku_id)
   AND r.warehouse_id=coalesce(b.warehouse_id,l.warehouse_id)
)
SELECT *,
       coalesce(on_hand,0)-coalesce(ledger_on_hand,0) AS on_hand_drift,
       coalesce(reserved,0)-coalesce(active_reserved,0) AS reservation_drift
FROM reconciled
WHERE coalesce(on_hand,0)<>coalesce(ledger_on_hand,0)
   OR coalesce(reserved,0)<>coalesce(active_reserved,0)
   OR expired_not_released>0
ORDER BY greatest(abs(coalesce(on_hand,0)-coalesce(ledger_on_hand,0)),
                  abs(coalesce(reserved,0)-coalesce(active_reserved,0))) DESC
LIMIT 200;

-- Candidate indexes: test one at a time and compare buffers + rows removed.
CREATE INDEX IF NOT EXISTS lab_inventory_ledger_dimension_time
ON inventory_ledger(sku_id,warehouse_id,created_at DESC) INCLUDE(delta,reason,reference_key);

CREATE INDEX IF NOT EXISTS lab_inventory_active_reservation
ON inventory_reservation(sku_id,warehouse_id,expires_at) INCLUDE(quantity)
WHERE status='RESERVED';
