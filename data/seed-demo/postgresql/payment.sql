-- Payment scenarios: success dominates, but FAILED/UNKNOWN/PARTIALLY_REFUNDED are present for recovery labs.
WITH scenario(seq,user_id,provider,amount,status) AS (VALUES
 (1,'demo-customer-2','MOMO',8000000::numeric,'PAID'),(2,'demo-customer-3','VNPAY',14990000,'PAID'),
 (3,'demo-customer-4','VIETQR',4000000,'PAID'),(4,'demo-customer-5','MOMO',11980000,'PAID'),
 (5,'demo-customer-6','VNPAY',2000000,'PARTIALLY_REFUNDED'),(6,'demo-customer-7','MOMO',17470000,'PAID'),
 (7,'demo-customer-8','VIETQR',6490000,'REFUNDED'),(8,'demo-customer-9','MOMO',3000000,'PAID'),
 (9,'demo-customer-10','VNPAY',23490000,'PAID'),(10,'demo-customer-11','MOMO',5000000,'PAID'),
 (11,'demo-customer-12','MOMO',7590000,'PENDING'),(12,'demo-customer-13','VNPAY',12090000,'UNKNOWN'),
 (17,'demo-customer-18','VIETQR',8500000,'FAILED'),(18,'demo-customer-19','MOMO',4200000,'PAID'),
 (19,'demo-customer-20','VNPAY',10180000,'PAID'),(20,'demo-customer-1','VIETQR',15500000,'PENDING')
)
INSERT INTO payment(payment_key,order_id,user_id,provider,amount,currency,status,provider_transaction_id,version,created_at,updated_at)
SELECT 'PAY-DEMO-'||lpad(seq::text,4,'0'),'ORD-DEMO-'||lpad(seq::text,4,'0'),user_id,provider,amount,'VND',status,
 CASE WHEN status IN ('PAID','PARTIALLY_REFUNDED','REFUNDED') THEN provider||'-DEMO-TX-'||seq ELSE NULL END,
 0,now()-make_interval(days=>seq*2),now()
FROM scenario
ON CONFLICT(payment_key) DO UPDATE SET status=EXCLUDED.status,provider_transaction_id=EXCLUDED.provider_transaction_id,updated_at=EXCLUDED.updated_at;

INSERT INTO payment_reconciliation_state(payment_id,retry_count,next_retry_at,last_error,updated_at)
SELECT id,2,now()+interval '5 minute','Provider timeout: payment result is UNKNOWN',now()
FROM payment WHERE payment_key='PAY-DEMO-0012'
ON CONFLICT(payment_id) DO UPDATE SET retry_count=EXCLUDED.retry_count,next_retry_at=EXCLUDED.next_retry_at,last_error=EXCLUDED.last_error,updated_at=EXCLUDED.updated_at;

INSERT INTO payment_attempt(payment_id,attempt_no,request_json,response_json,outcome,created_at)
SELECT p.id,a.attempt_no,jsonb_build_object('provider',p.provider,'attempt',a.attempt_no),
 CASE WHEN p.status='UNKNOWN' THEN NULL ELSE jsonb_build_object('status',p.status) END,
 CASE WHEN p.status='PAID' THEN 'CAPTURED' ELSE p.status END,p.created_at+make_interval(secs=>a.attempt_no*5)
FROM payment p CROSS JOIN LATERAL generate_series(1,CASE WHEN p.status IN ('UNKNOWN','FAILED') THEN 2 ELSE 1 END) a(attempt_no)
WHERE p.payment_key LIKE 'PAY-DEMO-%'
  AND NOT EXISTS (SELECT 1 FROM payment_attempt x WHERE x.payment_id=p.id AND x.attempt_no=a.attempt_no);

INSERT INTO refund(refund_key,payment_id,amount,status,provider_refund_id,created_at,updated_at)
SELECT 'REF-DEMO-PARTIAL',id,500000,'SUCCEEDED','RF-VNPAY-0005',now()-interval '3 day',now()
FROM payment WHERE payment_key='PAY-DEMO-0005' ON CONFLICT(refund_key) DO NOTHING;
INSERT INTO refund(refund_key,payment_id,amount,status,provider_refund_id,created_at,updated_at)
SELECT 'REF-DEMO-FULL',id,6490000,'SUCCEEDED','RF-VIETQR-0007',now()-interval '2 day',now()
FROM payment WHERE payment_key='PAY-DEMO-0007' ON CONFLICT(refund_key) DO NOTHING;
