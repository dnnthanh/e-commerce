-- Shared SKU range 2,000,001..3,000,000.
-- warehouse_weight, hot_sku, stockout_ratio, inventory_ledger.
WITH sku_source AS (
  SELECT 2000000+g AS sku_id,(g%100<8) AS hot_sku,
    CASE WHEN g%100<2 THEN 0 WHEN g%100<7 THEN 1+(g%5) WHEN g%100<20 THEN 8+(g%20) ELSE 30+(g%220) END AS base_stock,
    CASE WHEN g%100<34 THEN 1 WHEN g%100<64 THEN 2 WHEN g%100<76 THEN 3 WHEN g%100<86 THEN 4
         WHEN g%100<92 THEN 5 ELSE 6+(g%15) END AS warehouse_weight
  FROM generate_series(1,1000000) g
)
INSERT INTO inventory_balance(sku_id,warehouse_id,on_hand,reserved,version)
SELECT sku_id,warehouse_weight,base_stock,LEAST(base_stock,CASE WHEN hot_sku THEN sku_id%8 ELSE sku_id%3 END),0
FROM sku_source ON CONFLICT(sku_id,warehouse_id) DO NOTHING;

WITH hot AS (
  SELECT 2000000+g AS sku_id,CASE WHEN g%2=0 THEN 1 ELSE 2 END AS warehouse_id,80+(g%180) AS on_hand
  FROM generate_series(1,1000000) g WHERE g%100<8
)
INSERT INTO inventory_balance(sku_id,warehouse_id,on_hand,reserved,version)
SELECT sku_id,warehouse_id,on_hand,sku_id%10,0 FROM hot ON CONFLICT(sku_id,warehouse_id) DO NOTHING;

INSERT INTO inventory_ledger(sku_id,warehouse_id,delta,reason,reference_key,created_at)
SELECT sku_id,warehouse_id,on_hand,'OPENING','OPEN-'||sku_id||'-'||warehouse_id,now()-make_interval(days=>180+sku_id%180)
FROM inventory_balance WHERE sku_id BETWEEN 2000001 AND 3000000;

INSERT INTO inventory_ledger(sku_id,warehouse_id,delta,reason,reference_key,created_at)
SELECT 2000001+(g%1000000),CASE WHEN g%100<34 THEN 1 WHEN g%100<64 THEN 2 ELSE 3+(g%18) END,
  CASE WHEN g%100<78 THEN -(1+g%3) WHEN g%100<91 THEN 1+g%3 WHEN g%100<97 THEN 1 ELSE -(1+g%8) END,
  CASE WHEN g%100<78 THEN 'ORDER_CONFIRMED' WHEN g%100<91 THEN 'RESERVATION_RELEASED'
       WHEN g%100<97 THEN 'RETURN_ACCEPTED' ELSE 'CYCLE_COUNT_ADJUSTMENT' END,
  'MOVE-'||g,now()-make_interval(days=>g%365,hours=>CASE WHEN g%100<65 THEN 18+g%5 ELSE g%24 END)
FROM generate_series(1,2500000) g;
