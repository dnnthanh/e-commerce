SET STATISTICS IO,TIME ON;
SELECT h.seller_id,SUM(l.amount) amount,COUNT_BIG(*) c FROM dbo.lab_order_header h JOIN dbo.lab_order_line l ON l.order_id=h.id WHERE h.created_at>=DATEADD(day,-365,SYSUTCDATETIME()) GROUP BY h.seller_id ORDER BY amount DESC;
-- Inspect plan warnings for Hash/Sort spill and MemoryGrantInfo.
SELECT * FROM sys.dm_exec_query_memory_grants;
