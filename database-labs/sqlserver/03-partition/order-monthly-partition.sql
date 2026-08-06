USE order_db;
GO
-- Lab copy: keep production schema unchanged while learning partition elimination.
IF OBJECT_ID('order_history_lab','U') IS NOT NULL DROP TABLE order_history_lab;
IF EXISTS(SELECT 1 FROM sys.partition_schemes WHERE name='ps_order_month') DROP PARTITION SCHEME ps_order_month;
IF EXISTS(SELECT 1 FROM sys.partition_functions WHERE name='pf_order_month') DROP PARTITION FUNCTION pf_order_month;
GO
CREATE PARTITION FUNCTION pf_order_month(datetime2)
AS RANGE RIGHT FOR VALUES
('2025-01-01','2025-04-01','2025-07-01','2025-10-01','2026-01-01','2026-04-01','2026-07-01','2026-10-01');
GO
CREATE PARTITION SCHEME ps_order_month AS PARTITION pf_order_month ALL TO ([PRIMARY]);
GO
CREATE TABLE order_history_lab(
  id bigint NOT NULL,
  order_no varchar(64) NOT NULL,
  user_id varchar(64) NOT NULL,
  status varchar(32) NOT NULL,
  payable_amount decimal(19,2) NOT NULL,
  created_at datetime2 NOT NULL
) ON ps_order_month(created_at);
GO
INSERT INTO order_history_lab(id,order_no,user_id,status,payable_amount,created_at)
SELECT id,order_no,user_id,status,payable_amount,created_at FROM marketplace_order;
GO
CREATE INDEX IX_order_history_lab_user_time
ON order_history_lab(user_id,created_at DESC,id DESC)
INCLUDE(status,payable_amount)
ON ps_order_month(created_at);
GO
