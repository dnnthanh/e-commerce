CREATE OR ALTER PROC dbo.usp_lab_orders @seller_id bigint AS SELECT id,status,created_at FROM dbo.lab_order_header WHERE seller_id=@seller_id AND created_at>=DATEADD(day,-90,SYSUTCDATETIME()) ORDER BY created_at DESC;
EXEC sp_recompile 'dbo.usp_lab_orders'; EXEC dbo.usp_lab_orders @seller_id=42; EXEC dbo.usp_lab_orders @seller_id=999;
-- Capture actual plans and Query Store plan variants/compiled parameter values.
