-- Shared data set for deep PostgreSQL incidents. Run in a disposable lab database.
DROP TABLE IF EXISTS lab_order_line, lab_order_header, lab_outbox, lab_inventory, lab_product, lab_settlement CASCADE;
CREATE TABLE lab_order_header(id bigint generated always as identity primary key, seller_id bigint not null, customer_id bigint not null, status text not null, created_at timestamptz not null, payload jsonb);
CREATE TABLE lab_order_line(id bigint generated always as identity primary key, order_id bigint not null, sku_id bigint not null, qty int not null, amount numeric(14,2) not null, attributes jsonb);
CREATE TABLE lab_outbox(id bigint generated always as identity primary key, aggregate_id text not null, status text not null, event_type text not null, created_at timestamptz not null, processed_at timestamptz, payload jsonb);
CREATE TABLE lab_inventory(sku_id bigint not null, warehouse_id bigint not null, on_hand int not null, reserved int not null, version bigint not null, updated_at timestamptz not null, primary key(sku_id,warehouse_id));
CREATE TABLE lab_settlement(id bigint generated always as identity primary key, seller_id bigint not null, business_date date not null, gross numeric(14,2) not null, commission numeric(14,2) not null, payload text);
INSERT INTO lab_order_header(seller_id,customer_id,status,created_at,payload)
SELECT CASE WHEN g%10<6 THEN 999 ELSE 1+(g%2000) END, 1+(g%400000), CASE WHEN g%100<3 THEN 'PENDING' WHEN g%100<8 THEN 'CANCELLED' ELSE 'COMPLETED' END, now()-(g%730)*interval '1 day', jsonb_build_object('channel',CASE WHEN g%3=0 THEN 'MOBILE' ELSE 'WEB' END,'note',repeat('x',CASE WHEN g%50=0 THEN 12000 ELSE 40 END)) FROM generate_series(1,2000000) g;
INSERT INTO lab_order_line(order_id,sku_id,qty,amount,attributes) SELECT 1+(g%2000000),1+(g%250000),1+(g%3),(10000+(g%900000))::numeric,jsonb_build_object('brand','B'||(g%300),'color',CASE WHEN g%4=0 THEN 'black' ELSE 'other' END) FROM generate_series(1,5000000) g;
INSERT INTO lab_outbox(aggregate_id,status,event_type,created_at,processed_at,payload) SELECT 'O-'||g,CASE WHEN g>1950000 THEN 'PENDING' ELSE 'PROCESSED' END,'ORDER_CHANGED',now()-(g%60)*interval '1 minute',CASE WHEN g>1950000 THEN null ELSE now() END,jsonb_build_object('seq',g) FROM generate_series(1,2000000) g;
INSERT INTO lab_inventory SELECT g,1+(g%20),100+(g%200),g%20,1,now() FROM generate_series(1,500000) g;
INSERT INTO lab_settlement(seller_id,business_date,gross,commission,payload) SELECT CASE WHEN g%5=0 THEN 999 ELSE 1+(g%2000) END,current_date-(g%730),(10000+(g%900000))::numeric,(100+(g%9000))::numeric,repeat('s',100) FROM generate_series(1,3000000) g;
ANALYZE;
