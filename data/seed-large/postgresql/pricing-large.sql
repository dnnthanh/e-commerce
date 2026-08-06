-- 2M+ temporal price rules over the shared SKU range.
-- category_price_band, price_history_depth, scheduled_price.
WITH sku_source AS (
  SELECT 2000000+g AS sku_id,1+(((g-1)/2)%12) AS category_id,
    CASE WHEN g%100<55 THEN 10001+(g%20) WHEN g%100<85 THEN 10021+(g%80) ELSE 10101+(g%400) END AS seller_id,
    CASE 1+(((g-1)/2)%12) WHEN 1 THEN 7990000 WHEN 2 THEN 15990000 WHEN 3 THEN 8490000
      WHEN 4 THEN 1290000 WHEN 5 THEN 690000 WHEN 6 THEN 1490000 WHEN 7 THEN 790000
      WHEN 8 THEN 3990000 WHEN 9 THEN 990000 WHEN 10 THEN 1690000 WHEN 11 THEN 1890000 ELSE 1590000 END::numeric AS category_price_band
  FROM generate_series(1,1000000) g
), current_rule AS (
  SELECT *,round((category_price_band*(0.88+((sku_id%23)::numeric/100))*CASE WHEN sku_id%2=0 THEN 1.12 ELSE 1 END)/10000)*10000 AS amount
  FROM sku_source
)
INSERT INTO price_rule(sku_id,seller_id,currency,amount,valid_from,valid_to,priority,status,created_at)
SELECT sku_id,seller_id,'VND',amount,now()-make_interval(days=>10+sku_id%80),NULL,100,'ACTIVE',now()-make_interval(days=>10+sku_id%80)
FROM current_rule;

-- price_history_depth: one historical row per SKU; hot SKUs get a second historical row.
WITH base AS (SELECT sku_id,seller_id,amount FROM price_rule WHERE sku_id BETWEEN 2000001 AND 3000000 AND status='ACTIVE')
INSERT INTO price_rule(sku_id,seller_id,currency,amount,valid_from,valid_to,priority,status,created_at)
SELECT sku_id,seller_id,'VND',round(amount*1.06/10000)*10000,now()-interval '240 day',now()-interval '91 day',70,'INACTIVE',now()-interval '240 day' FROM base
UNION ALL
SELECT sku_id,seller_id,'VND',round(amount*1.03/10000)*10000,now()-interval '90 day',now()-interval '11 day',80,'INACTIVE',now()-interval '90 day' FROM base WHERE sku_id%100<8;

-- scheduled_price: a small future campaign cohort.
WITH base AS (SELECT sku_id,seller_id,amount FROM price_rule WHERE sku_id BETWEEN 2000001 AND 3000000 AND status='ACTIVE' AND valid_to IS NULL)
INSERT INTO price_rule(sku_id,seller_id,currency,amount,valid_from,valid_to,priority,status,created_at)
SELECT sku_id,seller_id,'VND',round(amount*0.93/10000)*10000,now()+interval '7 day',now()+interval '21 day',120,'ACTIVE',now()
FROM base WHERE sku_id%100<3;
