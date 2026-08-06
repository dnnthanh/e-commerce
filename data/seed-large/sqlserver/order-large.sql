USE order_db;
SET NOCOUNT ON;

-- 1,000,000 marketplace orders.
-- customer_frequency_weight, seller_popularity_weight, seasonality_weight, multi_seller_ratio.
IF NOT EXISTS (SELECT 1 FROM marketplace_order WHERE order_no LIKE 'ORD-L-%')
BEGIN
  ;WITH n AS (
    SELECT TOP (1000000) ROW_NUMBER() OVER(ORDER BY (SELECT NULL)) AS rn
    FROM sys.all_objects a CROSS JOIN sys.all_objects b CROSS JOIN sys.all_objects c
  ), source AS (
    SELECT rn,
      CASE WHEN rn%100<18 THEN 1+rn%5000 WHEN rn%100<62 THEN 5001+rn%45000 ELSE 50001+rn%200000 END AS customer_frequency_weight,
      CASE WHEN rn%100<42 THEN -(300+rn%60) ELSE rn%540 END AS seasonality_weight,
      CASE WHEN rn%1000<20 THEN 'CANCELLED' WHEN rn%1000<55 THEN 'CREATED' WHEN rn%1000<115 THEN 'PAYMENT_PENDING'
           WHEN rn%1000<390 THEN 'PAID' WHEN rn%1000<720 THEN 'FULFILLING' ELSE 'COMPLETED' END AS order_status,
      CAST(180000+((rn*7919)%140000)*300 AS decimal(19,2)) AS gross_amount
    FROM n
  )
  INSERT INTO marketplace_order(order_no,checkout_key,user_id,gross_amount,discount_amount,payable_amount,status,cancellation_reason,version,created_at,updated_at)
  SELECT CONCAT('ORD-L-',RIGHT('000000000'+CAST(rn AS varchar(9)),9)),
    CONCAT('CHK-L-',RIGHT('000000000'+CAST(rn AS varchar(9)),9)),
    CONCAT('customer-',RIGHT('0000000'+CAST(customer_frequency_weight AS varchar(7)),7)),gross_amount,
    CAST(CASE WHEN rn%100<42 THEN 0 ELSE IIF(gross_amount*0.10<60000+(rn%100)*6000,gross_amount*0.10,60000+(rn%100)*6000) END AS decimal(19,2)),
    gross_amount-CAST(CASE WHEN rn%100<42 THEN 0 ELSE IIF(gross_amount*0.10<60000+(rn%100)*6000,gross_amount*0.10,60000+(rn%100)*6000) END AS decimal(19,2)),
    order_status,
    CASE WHEN order_status='CANCELLED' THEN CASE rn%4 WHEN 0 THEN 'CUSTOMER_CHANGED_MIND' WHEN 1 THEN 'OUT_OF_STOCK' WHEN 2 THEN 'PAYMENT_TIMEOUT' ELSE 'SELLER_REJECTED' END ELSE NULL END,
    0,CASE WHEN seasonality_weight<0 THEN DATEADD(hour,18+rn%5,DATEFROMPARTS(2025,11+rn%2,1+rn%28)) ELSE DATEADD(hour,CASE WHEN rn%100<70 THEN 18+rn%5 ELSE rn%24 END,DATEADD(day,-seasonality_weight,CAST(CAST(GETDATE() AS date) AS datetime2))) END,SYSDATETIME()
  FROM source;

  -- seller_popularity_weight + multi_seller_ratio: 72% one seller, 23% two, 5% three.
  ;WITH orders AS (
    SELECT id,order_no,gross_amount,discount_amount,payable_amount,status,ABS(CONVERT(bigint,CHECKSUM(order_no))) AS h
    FROM marketplace_order WHERE order_no LIKE 'ORD-L-%'
  ), slots AS (
    SELECT o.*,v.slot,CASE WHEN h%100<72 THEN 1 WHEN h%100<95 THEN 2 ELSE 3 END AS multi_seller_ratio
    FROM orders o CROSS APPLY(VALUES(1),(2),(3)) v(slot)
  )
  INSERT INTO seller_order(order_id,seller_id,seller_order_no,gross_amount,discount_amount,payable_amount,status)
  SELECT id,
    CASE WHEN (h+slot)%100<55 THEN 10001+(h+slot)%20 WHEN (h+slot)%100<85 THEN 10021+(h+slot)%80 ELSE 10101+(h+slot)%400 END AS seller_popularity_weight,
    CONCAT('SORD-L-',RIGHT('000000000'+CAST(id AS varchar(9)),9),'-',slot),
    CAST(gross_amount/multi_seller_ratio AS decimal(19,2)),CAST(discount_amount/multi_seller_ratio AS decimal(19,2)),CAST(payable_amount/multi_seller_ratio AS decimal(19,2)),
    CASE status WHEN 'COMPLETED' THEN 'COMPLETED' WHEN 'FULFILLING' THEN 'FULFILLING' WHEN 'PAID' THEN 'PAID' WHEN 'CANCELLED' THEN 'CANCELLED' ELSE 'CREATED' END
  FROM slots WHERE slot<=multi_seller_ratio;

  -- line_total_reconciles: line monetary allocation is derived from seller-order totals.
  ;WITH seller_orders AS (
    SELECT id,seller_id,gross_amount,discount_amount,payable_amount,
           ABS(CONVERT(bigint,CHECKSUM(seller_order_no))) AS h,
           CASE WHEN ABS(CONVERT(bigint,CHECKSUM(seller_order_no)))%3=0 THEN 3 ELSE 2 END AS line_count
    FROM seller_order WHERE seller_order_no LIKE 'SORD-L-%'
  ), line_slots AS (
    SELECT s.*,v.line_no FROM seller_orders s CROSS APPLY(VALUES(1),(2),(3)) v(line_no) WHERE v.line_no<=s.line_count
  )
  INSERT INTO order_line(seller_order_id,sku_id,quantity,unit_price,allocated_discount,net_amount)
  SELECT id,
    CASE WHEN (h+line_no)%100<25 THEN 2000001+((h+line_no)%50000) ELSE 2000001+((h*17+line_no)%1000000) END,
    CASE WHEN line_no=2 AND line_count=3 THEN 2 ELSE 1 END,
    CAST((gross_amount/line_count)/(CASE WHEN line_no=2 AND line_count=3 THEN 2 ELSE 1 END) AS decimal(19,2)), -- gross_amount/line_count
    CAST(discount_amount/line_count AS decimal(19,2)),
    CAST(payable_amount/line_count AS decimal(19,2)) -- payable_amount/line_count
  FROM line_slots;
END;
