CREATE TABLE IF NOT EXISTS lab_promotion(id bigserial primary key,seller_id bigint not null,validity tstzrange not null,scope jsonb not null);
INSERT INTO lab_promotion(seller_id,validity,scope) SELECT 1+(g%2000),tstzrange(now()-(g%30)*interval '1 day',now()+(1+(g%15))*interval '1 day','[)'),jsonb_build_object('brand','B'||(g%300),'segment',CASE WHEN g%4=0 THEN 'VIP' ELSE 'ALL' END) FROM generate_series(1,500000) g ON CONFLICT DO NOTHING;
ANALYZE lab_promotion;
EXPLAIN (ANALYZE,BUFFERS) SELECT count(*) FROM lab_order_line WHERE attributes @> '{"brand":"B42"}'::jsonb;
EXPLAIN (ANALYZE,BUFFERS) SELECT * FROM lab_promotion WHERE seller_id=42 AND validity && tstzrange(now(),now()+interval '2 days','[)') AND scope @> '{"segment":"VIP"}'::jsonb;
