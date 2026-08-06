-- Shared MySQL 8.4 deep-incident dataset. Increase recursive limit only for bootstrap helper sequences if needed.
DROP TABLE IF EXISTS lab_order_line, lab_order_header, lab_outbox, lab_inventory;
CREATE TABLE lab_order_header(id BIGINT PRIMARY KEY AUTO_INCREMENT,seller_id BIGINT NOT NULL,customer_id BIGINT NOT NULL,status VARCHAR(32) NOT NULL,created_at DATETIME(6) NOT NULL,payload JSON,KEY idx_created(created_at));
CREATE TABLE lab_order_line(id BIGINT PRIMARY KEY AUTO_INCREMENT,order_id BIGINT NOT NULL,sku_id BIGINT NOT NULL,qty INT NOT NULL,amount DECIMAL(14,2) NOT NULL,description LONGTEXT,KEY idx_order(order_id));
CREATE TABLE lab_outbox(id BIGINT PRIMARY KEY AUTO_INCREMENT,aggregate_id VARCHAR(64) NOT NULL,status VARCHAR(32) NOT NULL,created_at DATETIME(6) NOT NULL,processed_at DATETIME(6),payload JSON);
CREATE TABLE lab_inventory(sku_id BIGINT NOT NULL,warehouse_id BIGINT NOT NULL,on_hand INT NOT NULL,reserved INT NOT NULL,version BIGINT NOT NULL,updated_at DATETIME(6) NOT NULL,PRIMARY KEY(sku_id,warehouse_id));
-- Seed with your preferred generator (sysbench, mysqlslap, recursive CTE chunks). Target shape:
-- 2M orders, 5M lines, 2M outbox rows, 500k inventory rows; seller 999 owns ~60% of orders.
-- Keep ~2.5% of outbox rows PENDING and make 2% of order-line descriptions 8-16KB to reproduce wide-row IO.
ANALYZE TABLE lab_order_header,lab_order_line,lab_outbox,lab_inventory;
