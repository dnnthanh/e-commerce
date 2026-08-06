SET STATISTICS IO ON;
SET STATISTICS TIME ON;
SELECT customer_id,status,SUM(total_amount) total_amount
FROM order_header
WHERE created_at>=DATEADD(day,-365,SYSUTCDATETIME())
GROUP BY customer_id,status
ORDER BY total_amount DESC;
-- Inspect actual plan spill warnings and sys.dm_exec_query_memory_grants. Fix cardinality/row reduction before server-wide memory changes.
