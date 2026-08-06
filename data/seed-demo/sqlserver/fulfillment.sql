USE fulfillment_db;
SET NOCOUNT ON;
-- partial_shipment scenarios and multiple Vietnamese carrier adapters.
DECLARE @shipment_seed TABLE(seq INT,part INT,seller_id BIGINT,warehouse_id BIGINT,carrier VARCHAR(64),status VARCHAR(32),age_days INT);
INSERT INTO @shipment_seed VALUES
(1,1,10002,1,'GHN','DELIVERED',2),(2,1,10003,2,'GHTK','DELIVERED',4),
(3,1,10004,1,'GHN','DELIVERED',6),(3,2,10009,3,'VIETTEL_POST','IN_TRANSIT',5), -- partial_shipment
(4,1,10005,2,'GHTK','DELIVERED',8),(5,1,10006,1,'GHN','RETURN_TO_SENDER',10),
(6,1,10007,3,'VIETTEL_POST','IN_TRANSIT',1),(6,2,10001,2,'GHN','READY_TO_SHIP',1), -- partial_shipment
(7,1,10008,1,'GHTK','DELIVERY_FAILED',2),(7,2,10002,4,'VIETTEL_POST','DELIVERED',14),
(8,1,10009,2,'GHN','READY_TO_SHIP',1),(9,1,10010,1,'VIETTEL_POST','IN_TRANSIT',2),
(10,1,10011,2,'GHTK','DELIVERED',20),(18,1,10007,1,'GHN','DELIVERED',22),(19,1,10008,2,'GHTK','DELIVERED',24);

INSERT INTO shipment(shipment_no,order_id,seller_id,warehouse_id,carrier_code,tracking_no,status,created_at,updated_at)
SELECT CONCAT('SHIP-DEMO-',RIGHT('0000'+CAST(seq AS varchar(4)),4),'-',part),
       CONCAT('ORD-DEMO-',RIGHT('0000'+CAST(seq AS varchar(4)),4)),seller_id,warehouse_id,carrier,
       CONCAT(carrier,'-',RIGHT('000000'+CAST(seq*10+part AS varchar(6)),6)),status,DATEADD(day,-age_days,SYSDATETIME()),SYSDATETIME()
FROM @shipment_seed s
WHERE NOT EXISTS (SELECT 1 FROM shipment x WHERE x.shipment_no=CONCAT('SHIP-DEMO-',RIGHT('0000'+CAST(s.seq AS varchar(4)),4),'-',s.part));

INSERT INTO shipment_item(shipment_id,order_line_id,sku_id,quantity)
SELECT sh.id,900000+s.seq*10+s.part,2001+((s.seq*17+s.part)%240),CASE WHEN s.seq%5=0 THEN 2 ELSE 1 END
FROM @shipment_seed s JOIN shipment sh ON sh.shipment_no=CONCAT('SHIP-DEMO-',RIGHT('0000'+CAST(s.seq AS varchar(4)),4),'-',s.part)
WHERE NOT EXISTS (SELECT 1 FROM shipment_item x WHERE x.shipment_id=sh.id);

-- Tracking history remains monotonic even when the final delivery failed or returned to sender.
INSERT INTO shipment_tracking(shipment_id,provider_sequence,status,occurred_at,source)
SELECT sh.id,v.seq_no,v.status,DATEADD(hour,v.seq_no*6,sh.created_at),'CARRIER_WEBHOOK'
FROM shipment sh
CROSS APPLY (VALUES(1,'ALLOCATED'),(2,'PICKING'),(3,'PACKED'),(4,'READY_TO_SHIP'),(5,'HANDED_OVER'),(6,'IN_TRANSIT')) v(seq_no,status)
WHERE sh.shipment_no LIKE 'SHIP-DEMO-%'
  AND v.seq_no<=CASE sh.status WHEN 'READY_TO_SHIP' THEN 4 WHEN 'DELIVERY_FAILED' THEN 6 WHEN 'RETURN_TO_SENDER' THEN 6 WHEN 'IN_TRANSIT' THEN 6 ELSE 6 END
  AND NOT EXISTS (SELECT 1 FROM shipment_tracking t WHERE t.shipment_id=sh.id AND t.provider_sequence=v.seq_no);

INSERT INTO shipment_tracking(shipment_id,provider_sequence,status,occurred_at,source)
SELECT sh.id,7,CASE WHEN sh.status='RETURN_TO_SENDER' THEN 'DELIVERY_FAILED' ELSE sh.status END,DATEADD(hour,-1,sh.updated_at),'CARRIER_WEBHOOK' FROM shipment sh
WHERE sh.shipment_no LIKE 'SHIP-DEMO-%' AND sh.status IN ('DELIVERED','DELIVERY_FAILED','RETURN_TO_SENDER')
  AND NOT EXISTS (SELECT 1 FROM shipment_tracking t WHERE t.shipment_id=sh.id AND t.provider_sequence=7);

INSERT INTO shipment_tracking(shipment_id,provider_sequence,status,occurred_at,source)
SELECT sh.id,8,'RETURN_TO_SENDER',sh.updated_at,'CARRIER_WEBHOOK' FROM shipment sh
WHERE sh.shipment_no LIKE 'SHIP-DEMO-%' AND sh.status='RETURN_TO_SENDER'
  AND NOT EXISTS (SELECT 1 FROM shipment_tracking t WHERE t.shipment_id=sh.id AND t.provider_sequence=8);
