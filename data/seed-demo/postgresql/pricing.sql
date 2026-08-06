-- Category-specific temporal prices; not linear cloned values.
WITH sku_seed AS (
  SELECT sku_id,1001 + ((sku_id - 2001) / 2) AS product_id,
         1 + (((1001 + ((sku_id - 2001) / 2)) - 1001) % 12) AS category_id,
         ((sku_id - 2001) % 2) AS variant_no
  FROM generate_series(2001,2240) AS sku_id
), priced AS (
  SELECT *,CASE category_id WHEN 1 THEN 7990000 WHEN 2 THEN 15990000 WHEN 3 THEN 8490000
    WHEN 4 THEN 1290000 WHEN 5 THEN 690000 WHEN 6 THEN 1490000 WHEN 7 THEN 790000
    WHEN 8 THEN 3990000 WHEN 9 THEN 990000 WHEN 10 THEN 1690000 WHEN 11 THEN 1890000
    ELSE 1590000 END::numeric AS category_base
  FROM sku_seed
)
INSERT INTO price_rule(sku_id,seller_id,currency,amount,valid_from,valid_to,priority,status,created_at)
SELECT sku_id,10000 + category_id,'VND',
  round((category_base + ((product_id - 1001) / 12) * CASE WHEN category_id IN (1,2,3,8) THEN 250000 ELSE 50000 END
       + variant_no * CASE WHEN category_id IN (1,2,3,8,12) THEN category_base * 0.18 ELSE category_base * 0.08 END) / 10000) * 10000,
  now()-interval '30 day',NULL,100,'ACTIVE',now()-interval '30 day'
FROM priced;

WITH history AS (SELECT sku_id,seller_id,amount FROM price_rule WHERE sku_id BETWEEN 2001 AND 2240 AND sku_id % 5 = 0)
INSERT INTO price_rule(sku_id,seller_id,currency,amount,valid_from,valid_to,priority,status,created_at)
SELECT sku_id,seller_id,'VND',round(amount*1.07/10000)*10000,now()-interval '120 day',now()-interval '31 day',80,'INACTIVE',now()-interval '120 day'
FROM history;

WITH scheduled AS (SELECT sku_id,seller_id,amount FROM price_rule WHERE sku_id BETWEEN 2001 AND 2240 AND status='ACTIVE' AND sku_id % 17 = 0)
INSERT INTO price_rule(sku_id,seller_id,currency,amount,valid_from,valid_to,priority,status,created_at)
SELECT sku_id,seller_id,'VND',round(amount*0.95/10000)*10000,now()+interval '7 day',now()+interval '21 day',120,'ACTIVE',now()
FROM scheduled;
