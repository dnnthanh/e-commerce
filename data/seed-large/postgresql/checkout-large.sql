-- Durable checkout workflows with abandonment, retryable failures and evening peaks.
-- abandon_ratio / abandon_before_checkout_ratio belongs to Cart; compensated_ratio, retryable_ratio, peak_hour_weight are modeled here.
WITH source AS (
  SELECT g,
    CASE WHEN g%10000<12 THEN 'PAYMENT_UNKNOWN' WHEN g%1000<55 THEN 'COMPENSATED'
         WHEN g%1000<67 THEN 'FAILED_RETRYABLE' WHEN g%1000<125 THEN 'PAYMENT_PENDING' ELSE 'COMPLETED' END AS state,
    CASE WHEN g%100<70 THEN 18+(g%5) WHEN g%100<88 THEN 11+(g%3) ELSE g%24 END AS peak_hour_weight,
    1+(g%5) AS item_count,100000+((g*7919)%35000000) AS gross
  FROM generate_series(1,1000000) g
)
INSERT INTO checkout_saga(checkout_key,user_id,status,cart_snapshot_json,pricing_snapshot_json,reservation_json,order_id,payment_id,retry_count,next_retry_at,created_at,updated_at)
SELECT 'CHK-L-'||lpad(g::text,9,'0'),'customer-'||lpad((1+(g%250000))::text,7,'0'),state,
  jsonb_build_object('itemCount',item_count,'selectedSellerCount',CASE WHEN g%100<72 THEN 1 WHEN g%100<95 THEN 2 ELSE 3 END,'source',CASE WHEN g%100<72 THEN 'mobile' ELSE 'web' END),
  jsonb_build_object('gross',gross,'discount',LEAST(gross*0.12,(g%400000)),'currency','VND'),
  CASE WHEN state='COMPENSATED' THEN jsonb_build_object('promotionReleased',true,'inventoryReleased',true) ELSE jsonb_build_object('inventoryKey','INV-L-'||g,'promotionKey','PROMO-L-'||g) END,
  CASE WHEN state IN('COMPLETED','PAYMENT_PENDING','PAYMENT_UNKNOWN') THEN 'ORD-L-'||lpad(g::text,9,'0') ELSE NULL END,
  CASE WHEN state IN('COMPLETED','PAYMENT_PENDING','PAYMENT_UNKNOWN') THEN 'PAY-L-'||lpad(g::text,9,'0') ELSE NULL END,
  CASE WHEN state IN('FAILED_RETRYABLE','PAYMENT_UNKNOWN') THEN 1+(g%4) ELSE 0 END,
  CASE WHEN state IN('FAILED_RETRYABLE','PAYMENT_UNKNOWN') THEN now()+make_interval(mins=>5+(g%30)) ELSE NULL END,
  now()-make_interval(days=>g%180,hours=>23-peak_hour_weight),now()
FROM source ON CONFLICT(checkout_key) DO NOTHING;
