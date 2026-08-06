CREATE INDEX IX_lab_order_status_cover ON dbo.lab_order_header(status,created_at DESC) INCLUDE(seller_id,customer_id);
SELECT id,seller_id,customer_id,status,created_at FROM dbo.lab_order_header WHERE status='PENDING' ORDER BY created_at DESC;
-- Keep nvarchar(max) payload out of INCLUDE. Fetch details by id only after user selects a row.
