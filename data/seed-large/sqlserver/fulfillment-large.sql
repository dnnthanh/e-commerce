USE fulfillment_db;
;WITH n AS (SELECT TOP (1000000) ROW_NUMBER() OVER(ORDER BY (SELECT NULL)) AS rn FROM sys.all_objects a CROSS JOIN sys.all_objects b)
INSERT INTO shipment(shipment_no,order_id,seller_id,warehouse_id,carrier_code,tracking_no,status,created_at,updated_at)
SELECT CONCAT('SHP-L-',rn),CONCAT('ORD-L-',rn),10000+(rn%500),1+(rn%20),'LOCAL',CONCAT('TRK-',rn),CASE rn%5 WHEN 0 THEN 'DELIVERED' WHEN 1 THEN 'SHIPPED' ELSE 'PACKING' END,DATEADD(day,-(rn%60),SYSDATETIME()),SYSDATETIME() FROM n;
