-- Performance dataset: 500,000 return requests + 1,000,000 return lines.
INSERT INTO return_request(return_key,order_id,user_id,status,reason,refundable_amount,created_at,updated_at)
SELECT
    'RET-PERF-' || lpad(g::text, 9, '0'),
    'ORD-PERF-' || lpad(g::text, 9, '0'),
    'perf-user-' || (g % 100000),
    (ARRAY['CREATED','APPROVED','RECEIVED','REFUND_PENDING','COMPLETED'])[1 + (g % 5)],
    (ARRAY['DAMAGED','WRONG_ITEM','NOT_AS_DESCRIBED','CHANGED_MIND'])[1 + (g % 4)],
    100000 + ((g * 3571) % 10000000),
    now() - ((g % 180) || ' day')::interval,
    now()
FROM generate_series(1, 500000) AS g;

INSERT INTO return_line(return_id,order_line_id,seller_id,sku_id,quantity,refundable_amount,inspection_status)
SELECT r.id, 3000000 + r.id * 2, 10001 + (r.id % 500), 2001 + (r.id % 1000000), 1,
       round((r.refundable_amount * 0.55)::numeric, 2),
       CASE WHEN r.id % 3 = 0 THEN 'ACCEPTED' ELSE NULL END
FROM return_request r WHERE r.return_key LIKE 'RET-PERF-%'
UNION ALL
SELECT r.id, 3000001 + r.id * 2, 10001 + ((r.id + 1) % 500), 2001 + ((r.id + 1) % 1000000), 1,
       round((r.refundable_amount * 0.45)::numeric, 2),
       CASE WHEN r.id % 3 = 0 THEN 'ACCEPTED' ELSE NULL END
FROM return_request r WHERE r.return_key LIKE 'RET-PERF-%';
