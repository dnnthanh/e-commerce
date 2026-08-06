-- 1M payments with rare failure states and realistic provider/time skew.
-- success_ratio, unknown_ratio, provider_weight, business_hour_weight.
WITH source AS (
  SELECT g,
    CASE WHEN g%100<46 THEN 'MOMO' WHEN g%100<79 THEN 'VNPAY' ELSE 'VIETQR' END AS provider_weight,
    CASE WHEN g%10000<18 THEN 'UNKNOWN' WHEN g%1000<14 THEN 'FAILED' WHEN g%1000<35 THEN 'PENDING'
         WHEN g%1000<43 THEN 'PARTIALLY_REFUNDED' WHEN g%1000<47 THEN 'REFUNDED' ELSE 'PAID' END AS state,
    CASE WHEN g%100<68 THEN 18+(g%5) WHEN g%100<86 THEN 11+(g%3) ELSE g%24 END AS business_hour_weight,
    100000+((g*7919)%45000000) AS amount
  FROM generate_series(1,1000000) g
)
INSERT INTO payment(payment_key,order_id,user_id,provider,amount,currency,status,provider_transaction_id,version,created_at,updated_at)
SELECT 'PAY-L-'||lpad(g::text,9,'0'),'ORD-L-'||lpad(g::text,9,'0'),
  'customer-'||lpad((1+(g%250000))::text,7,'0'),provider_weight,amount,'VND',state,
  CASE WHEN state IN('PAID','PARTIALLY_REFUNDED','REFUNDED') THEN provider_weight||'-TX-'||lpad(g::text,12,'0') ELSE NULL END,
  0,now()-make_interval(days=>g%365,hours=>23-business_hour_weight),now()
FROM source ON CONFLICT(payment_key) DO NOTHING;

-- unknown_ratio payments are picked up by reconciliation workers.
INSERT INTO payment_reconciliation_state(payment_id,retry_count,next_retry_at,last_error,updated_at)
SELECT id,1+(id%3),now()+make_interval(mins=>5+id%30),'Provider timeout; final outcome not confirmed',now()
FROM payment WHERE payment_key LIKE 'PAY-L-%' AND status='UNKNOWN' ON CONFLICT(payment_id) DO NOTHING;

-- success_ratio rows usually have one attempt; UNKNOWN/FAILED rows have multiple attempts.
INSERT INTO payment_attempt(payment_id,attempt_no,request_json,response_json,outcome,created_at)
SELECT p.id,a.attempt_no,jsonb_build_object('orderId',p.order_id,'provider',p.provider,'attempt',a.attempt_no),
  CASE WHEN p.status='UNKNOWN' THEN NULL ELSE jsonb_build_object('status',p.status) END,
  CASE WHEN p.status='PAID' THEN 'CAPTURED' ELSE p.status END,p.created_at+make_interval(secs=>a.attempt_no*7)
FROM payment p CROSS JOIN LATERAL generate_series(1,CASE WHEN p.status IN('FAILED','UNKNOWN') THEN 2+(p.id%2)::int ELSE 1 END) a(attempt_no)
WHERE p.payment_key LIKE 'PAY-L-%';
