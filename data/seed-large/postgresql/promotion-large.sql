-- Performance dataset: 250,000 promotions + 500,000 condition rows.
INSERT INTO promotion(code,name,promotion_type,stacking_group,priority,start_at,end_at,status)
SELECT
    'PERF-PROMO-' || lpad(g::text, 8, '0'),
    'Synthetic campaign ' || g,
    CASE WHEN g % 3 = 0 THEN 'PERCENT' WHEN g % 3 = 1 THEN 'FIXED_AMOUNT' ELSE 'FREE_SHIPPING' END,
    'GROUP-' || (g % 50),
    1 + (g % 100),
    now() - ((g % 60) || ' day')::interval,
    now() + ((g % 90 + 1) || ' day')::interval,
    CASE WHEN g % 20 = 0 THEN 'INACTIVE' ELSE 'ACTIVE' END
FROM generate_series(1, 250000) AS g;

INSERT INTO promotion_condition(promotion_id,condition_type,condition_json)
SELECT p.id,'MIN_ORDER',jsonb_build_object('amount',100000 + ((p.id * 7919) % 5000000))
FROM promotion p WHERE p.code LIKE 'PERF-PROMO-%'
UNION ALL
SELECT p.id,'SELLER_SCOPE',jsonb_build_object('sellerId',10001 + (p.id % 500))
FROM promotion p WHERE p.code LIKE 'PERF-PROMO-%';
