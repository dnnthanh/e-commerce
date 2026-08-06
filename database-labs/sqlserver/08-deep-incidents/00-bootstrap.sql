-- Shared SQL Server 2022 deep-incident dataset.
DROP TABLE IF EXISTS dbo.lab_order_line,dbo.lab_order_header,dbo.lab_outbox,dbo.lab_settlement;
CREATE TABLE dbo.lab_order_header(id bigint IDENTITY PRIMARY KEY,seller_id bigint NOT NULL,customer_id bigint NOT NULL,status varchar(32) NOT NULL,created_at datetime2(6) NOT NULL,payload nvarchar(max));
CREATE TABLE dbo.lab_order_line(id bigint IDENTITY PRIMARY KEY,order_id bigint NOT NULL,sku_id bigint NOT NULL,qty int NOT NULL,amount decimal(14,2) NOT NULL,description nvarchar(max));
CREATE TABLE dbo.lab_outbox(id bigint IDENTITY PRIMARY KEY,aggregate_id varchar(64) NOT NULL,status varchar(32) NOT NULL,created_at datetime2(6) NOT NULL,processed_at datetime2(6),payload nvarchar(max));
CREATE TABLE dbo.lab_settlement(id bigint IDENTITY PRIMARY KEY,seller_id bigint NOT NULL,business_date date NOT NULL,gross decimal(14,2) NOT NULL,commission decimal(14,2) NOT NULL);
-- Use a numbers table or cross joins to seed >=2M headers and >=5M lines.
-- Deliberately skew seller_id=999 to 60% and keep a long tail of ~2000 sellers.
-- Make status correlated with seller 999, and retain wide payloads on ~2% rows.
UPDATE STATISTICS dbo.lab_order_header WITH FULLSCAN;
