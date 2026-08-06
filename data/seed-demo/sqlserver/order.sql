USE order_db;
SET NOCOUNT ON;

-- Reconciliation-safe demo orders: parent = sum(seller orders) and seller order = sum(lines).
-- Cross-service keys intentionally match CHK-DEMO/PAY-DEMO identifiers.
DECLARE @order_seed TABLE(
  seq INT PRIMARY KEY,
  user_id VARCHAR(64),
  status VARCHAR(32),
  gross_amount DECIMAL(19,2),
  discount_amount DECIMAL(19,2),
  cancellation_reason VARCHAR(32),
  seller_count INT,
  age_days INT
);
INSERT INTO @order_seed VALUES
(1,'demo-customer-2','COMPLETED',8400000,600000,NULL,1,2),
(2,'demo-customer-3','COMPLETED',15000000,900000,NULL,1,4),
(3,'demo-customer-4','COMPLETED',4200000,300000,NULL,2,6),
(4,'demo-customer-5','COMPLETED',12000000,0,NULL,1,8),
(5,'demo-customer-6','COMPLETED',2400000,240000,NULL,1,10),
(6,'demo-customer-7','FULFILLING',18000000,1200000,NULL,2,12),
(7,'demo-customer-8','COMPLETED',6600000,600000,NULL,2,14),
(8,'demo-customer-9','PAID',3600000,300000,NULL,1,16),
(9,'demo-customer-10','FULFILLING',24000000,1500000,NULL,2,18),
(10,'demo-customer-11','COMPLETED',5400000,480000,NULL,1,20),
(11,'demo-customer-12','PAYMENT_PENDING',7800000,420000,NULL,1,1),
(12,'demo-customer-13','PAYMENT_PENDING',12600000,900000,NULL,2,1),
(17,'demo-customer-18','CANCELLED',9000000,480000,'PAYMENT_TIMEOUT',1,3),
(18,'demo-customer-19','COMPLETED',4800000,420000,NULL,1,22),
(19,'demo-customer-20','COMPLETED',10800000,840000,NULL,2,24),
(20,'demo-customer-1','PAYMENT_PENDING',16800000,1260000,NULL,2,1);

INSERT INTO marketplace_order(order_no,checkout_key,user_id,gross_amount,discount_amount,payable_amount,status,cancellation_reason,version,created_at,updated_at)
SELECT CONCAT('ORD-DEMO-',RIGHT('0000'+CAST(s.seq AS varchar(4)),4)),
       CONCAT('CHK-DEMO-',RIGHT('0000'+CAST(s.seq AS varchar(4)),4)),
       s.user_id,s.gross_amount,s.discount_amount,s.gross_amount-s.discount_amount,s.status,s.cancellation_reason,0,
       DATEADD(day,-s.age_days,DATEADD(hour,CASE WHEN s.seq%3=0 THEN -19 ELSE -13 END,SYSDATETIME())),SYSDATETIME()
FROM @order_seed s
WHERE NOT EXISTS (SELECT 1 FROM marketplace_order o WHERE o.order_no=CONCAT('ORD-DEMO-',RIGHT('0000'+CAST(s.seq AS varchar(4)),4)));

;WITH base AS (
  SELECT o.id,o.order_no,s.seq,s.status,s.seller_count,s.gross_amount,s.discount_amount,
         s.gross_amount-s.discount_amount AS payable_amount
  FROM @order_seed s JOIN marketplace_order o
    ON o.order_no=CONCAT('ORD-DEMO-',RIGHT('0000'+CAST(s.seq AS varchar(4)),4))
), slots AS (
  SELECT b.*,v.slot FROM base b CROSS APPLY(VALUES(1),(2)) v(slot) WHERE v.slot<=b.seller_count
)
INSERT INTO seller_order(order_id,seller_id,seller_order_no,gross_amount,discount_amount,payable_amount,status)
SELECT id,
       CASE WHEN (seq+slot)%5=0 THEN 10001 WHEN (seq+slot)%5=1 THEN 10002 WHEN (seq+slot)%5=2 THEN 10005 WHEN (seq+slot)%5=3 THEN 10008 ELSE 10011 END,
       CONCAT('SORD-DEMO-',RIGHT('0000'+CAST(seq AS varchar(4)),4),'-',slot),
       gross_amount/seller_count,discount_amount/seller_count,payable_amount/seller_count,
       CASE status WHEN 'COMPLETED' THEN 'COMPLETED' WHEN 'FULFILLING' THEN 'FULFILLING' WHEN 'PAID' THEN 'PAID' WHEN 'CANCELLED' THEN 'CANCELLED' ELSE 'CREATED' END
FROM slots s
WHERE NOT EXISTS (SELECT 1 FROM seller_order x WHERE x.seller_order_no=CONCAT('SORD-DEMO-',RIGHT('0000'+CAST(s.seq AS varchar(4)),4),'-',s.slot));

-- line_total_reconciles: gross_amount/line_count and payable_amount/line_count preserve seller totals.
;WITH seller_seed AS (
  SELECT so.id,so.seller_order_no,so.gross_amount,so.discount_amount,so.payable_amount,
         CASE WHEN ABS(CONVERT(bigint,CHECKSUM(so.seller_order_no)))%3=0 THEN 3 ELSE 2 END AS line_count,
         ABS(CONVERT(bigint,CHECKSUM(so.seller_order_no))) AS h
  FROM seller_order so WHERE so.seller_order_no LIKE 'SORD-DEMO-%'
), line_slots AS (
  SELECT s.*,v.line_no FROM seller_seed s CROSS APPLY(VALUES(1),(2),(3)) v(line_no) WHERE v.line_no<=s.line_count
)
INSERT INTO order_line(seller_order_id,sku_id,quantity,unit_price,allocated_discount,net_amount)
SELECT id,2001+((h+line_no*17)%240),
       CASE WHEN line_no=2 AND line_count=3 THEN 2 ELSE 1 END AS quantity,
       CAST((gross_amount/line_count)/(CASE WHEN line_no=2 AND line_count=3 THEN 2 ELSE 1 END) AS decimal(19,2)),
       CAST(discount_amount/line_count AS decimal(19,2)),
       CAST(payable_amount/line_count AS decimal(19,2))
FROM line_slots l
WHERE NOT EXISTS (SELECT 1 FROM order_line x WHERE x.seller_order_id=l.id);
