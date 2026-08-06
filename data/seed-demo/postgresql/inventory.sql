-- Hot SKUs are multi-warehouse; dataset includes stockout, low-stock and reserved examples.
WITH sku_stock AS (
  SELECT sku_id,1 + ((sku_id - 2001) % 5) AS primary_warehouse,
    CASE WHEN sku_id % 37 = 0 THEN 0 WHEN sku_id % 19 = 0 THEN 3 WHEN sku_id % 7 = 0 THEN 18
         WHEN sku_id % 5 = 0 THEN 42 ELSE 75 + ((sku_id * 13) % 120) END AS on_hand,
    CASE WHEN sku_id % 11 = 0 THEN 4 WHEN sku_id % 7 = 0 THEN 2 ELSE 0 END AS reserved
  FROM generate_series(2001,2240) AS sku_id
)
INSERT INTO inventory_balance(sku_id,warehouse_id,on_hand,reserved,version)
SELECT sku_id,primary_warehouse,on_hand,LEAST(reserved,on_hand),0 FROM sku_stock
ON CONFLICT (sku_id,warehouse_id) DO NOTHING;

WITH hot AS (
  SELECT sku_id,CASE WHEN sku_id % 2 = 0 THEN 1 ELSE 2 END AS secondary_warehouse,25 + (sku_id % 45) AS on_hand
  FROM generate_series(2001,2240) AS sku_id WHERE sku_id % 13 IN (0,1,2)
)
INSERT INTO inventory_balance(sku_id,warehouse_id,on_hand,reserved,version)
SELECT sku_id,secondary_warehouse,on_hand,CASE WHEN sku_id % 3=0 THEN 2 ELSE 0 END,0 FROM hot
ON CONFLICT (sku_id,warehouse_id) DO NOTHING;

INSERT INTO inventory_ledger(sku_id,warehouse_id,delta,reason,reference_key,created_at)
SELECT sku_id,warehouse_id,on_hand,'OPENING','DEMO-OPEN-'||sku_id||'-'||warehouse_id,
       now()-make_interval(days => 20 + (sku_id % 15))
FROM inventory_balance WHERE sku_id BETWEEN 2001 AND 2240;

INSERT INTO inventory_ledger(sku_id,warehouse_id,delta,reason,reference_key,created_at)
SELECT b.sku_id,b.warehouse_id,CASE WHEN b.sku_id % 9=0 THEN 1 ELSE -(1+b.sku_id%3) END,
       CASE WHEN b.sku_id % 9=0 THEN 'RETURN_ACCEPTED' ELSE 'ORDER_CONFIRMED' END,
       'DEMO-MOVE-'||b.sku_id||'-'||b.warehouse_id,now()-make_interval(hours => b.sku_id%96)
FROM inventory_balance b WHERE b.sku_id BETWEEN 2001 AND 2240 AND b.on_hand>0 AND b.sku_id%4=0;
