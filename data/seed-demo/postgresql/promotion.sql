INSERT INTO promotion(code,name,promotion_type,stacking_group,priority,start_at,end_at,status) VALUES
('WELCOME100','Giảm 100K đơn đầu','FIXED_ORDER','WELCOME',100,now()-interval '1 day',now()+interval '365 day','ACTIVE'),
('FLASH10','Flash Sale 10%','PERCENT_ITEM','FLASH',200,now()-interval '1 hour',now()+interval '7 day','ACTIVE') ON CONFLICT DO NOTHING;
INSERT INTO promotion_condition(promotion_id,condition_type,condition_json)
SELECT id,'MIN_ORDER','{"amount":1000000}'::jsonb FROM promotion WHERE code='WELCOME100';
