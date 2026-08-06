CREATE INDEX IX_lab_order_seller_created ON dbo.lab_order_header(seller_id,created_at DESC) INCLUDE(status);
-- Compare SQL Server 2022 PSP, OPTION(RECOMPILE) at statement scope, and OPTIMIZE FOR UNKNOWN. Choose based on workload/compile budget.
SELECT id,status,created_at FROM dbo.lab_order_header WHERE seller_id=@seller_id ORDER BY created_at DESC OPTION(RECOMPILE);
