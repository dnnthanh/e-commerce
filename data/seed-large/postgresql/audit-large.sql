INSERT INTO audit_event(event_id,actor_id,actor_type,action,resource_type,resource_id,source_service,trace_id,before_json,after_json,reason,occurred_at)
SELECT gen_random_uuid()::text,'admin-'||(g%100),'USER',CASE g%4 WHEN 0 THEN 'SELLER_UPDATED' WHEN 1 THEN 'PERMISSION_CHANGED' WHEN 2 THEN 'DLT_REPLAYED' ELSE 'PAYMENT_RECONCILED' END,
CASE g%3 WHEN 0 THEN 'SELLER' WHEN 1 THEN 'USER' ELSE 'PAYMENT' END,g::text,'be-audit-seed','trace-'||g,NULL,jsonb_build_object('sequence',g),NULL,now()-(g%365)*interval '1 day'
FROM generate_series(1,1000000) g;
