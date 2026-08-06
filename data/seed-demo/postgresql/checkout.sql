-- Production-like checkout scenarios. Abandoned carts stay in Cart; checkout uses valid saga states.
-- channel, promotionReserved, inventoryReserved make compensation state visible for learning.
WITH scenario(seq,user_id,status,channel,item_count,gross_amount,discount_amount,retry_count) AS (VALUES
 (1,'demo-customer-2','COMPLETED','mobile',2,8490000,490000,0),
 (2,'demo-customer-3','COMPLETED','web',1,15990000,1000000,0),
 (3,'demo-customer-4','COMPLETED','mobile',3,4270000,270000,0),
 (4,'demo-customer-5','COMPLETED','web',2,11980000,0,0),
 (5,'demo-customer-6','COMPLETED','mobile',1,2190000,190000,0),
 (6,'demo-customer-7','COMPLETED','mobile',4,18670000,1200000,0),
 (7,'demo-customer-8','COMPLETED','web',2,6990000,500000,0),
 (8,'demo-customer-9','COMPLETED','mobile',3,3290000,290000,0),
 (9,'demo-customer-10','COMPLETED','web',1,24990000,1500000,0),
 (10,'demo-customer-11','COMPLETED','mobile',2,5480000,480000,0),
 (11,'demo-customer-12','PAYMENT_PENDING','mobile',2,7990000,400000,0),
 (12,'demo-customer-13','PAYMENT_UNKNOWN','web',1,12990000,900000,2),
 (13,'demo-customer-14','FAILED_RETRYABLE','mobile',3,3780000,280000,1),
 (14,'demo-customer-15','COMPENSATED','web',2,6290000,490000,1),
 (15,'demo-customer-16','STARTED','mobile',1,1890000,0,0),
 (16,'demo-customer-17','RESERVED','web',3,14670000,1200000,0),
 (17,'demo-customer-18','ORDER_CREATED','mobile',2,9280000,780000,0),
 (18,'demo-customer-19','COMPLETED','web',1,4590000,390000,0),
 (19,'demo-customer-20','COMPLETED','mobile',2,10980000,800000,0),
 (20,'demo-customer-1','PAYMENT_PENDING','web',4,16760000,1260000,0)
)
INSERT INTO checkout_saga(checkout_key,user_id,status,cart_snapshot_json,pricing_snapshot_json,reservation_json,order_id,payment_id,retry_count,next_retry_at,created_at,updated_at)
SELECT 'CHK-DEMO-'||lpad(seq::text,4,'0'),user_id,status,
 jsonb_build_object('channel',channel,'itemCount',item_count,'selectedSellerCount',CASE WHEN item_count>=3 THEN 2 ELSE 1 END),
 jsonb_build_object('gross',gross_amount,'discount',discount_amount,'payable',gross_amount-discount_amount,'currency','VND','quoteVersion',1),
 jsonb_build_object(
   'promotionReserved',status IN ('RESERVED','ORDER_CREATED','PAYMENT_PENDING','PAYMENT_UNKNOWN','COMPLETED'),
   'inventoryReserved',status IN ('RESERVED','ORDER_CREATED','PAYMENT_PENDING','PAYMENT_UNKNOWN','COMPLETED'),
   'compensated',status='COMPENSATED'),
 CASE WHEN status IN ('ORDER_CREATED','PAYMENT_PENDING','PAYMENT_UNKNOWN','COMPLETED') THEN 'ORD-DEMO-'||lpad(seq::text,4,'0') ELSE NULL END,
 CASE WHEN status IN ('PAYMENT_PENDING','PAYMENT_UNKNOWN','COMPLETED') THEN 'PAY-DEMO-'||lpad(seq::text,4,'0') ELSE NULL END,
 retry_count,CASE WHEN status IN ('FAILED_RETRYABLE','PAYMENT_UNKNOWN') THEN now()+interval '10 minute' ELSE NULL END,
 now()-make_interval(days=>seq*2,hours=>CASE WHEN channel='mobile' THEN 20 ELSE 13 END),now()
FROM scenario
ON CONFLICT(checkout_key) DO UPDATE SET status=EXCLUDED.status,cart_snapshot_json=EXCLUDED.cart_snapshot_json,
 pricing_snapshot_json=EXCLUDED.pricing_snapshot_json,reservation_json=EXCLUDED.reservation_json,order_id=EXCLUDED.order_id,
 payment_id=EXCLUDED.payment_id,retry_count=EXCLUDED.retry_count,next_retry_at=EXCLUDED.next_retry_at,updated_at=EXCLUDED.updated_at;
