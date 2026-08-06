ALTER DATABASE CURRENT SET QUERY_STORE = ON;
SELECT seller_id,status,COUNT_BIG(*) FROM dbo.lab_order_header WHERE created_at>=DATEADD(day,-90,SYSUTCDATETIME()) GROUP BY seller_id,status;
SELECT q.query_id,p.plan_id,rs.avg_duration,rs.avg_logical_io_reads FROM sys.query_store_query q JOIN sys.query_store_plan p ON p.query_id=q.query_id JOIN sys.query_store_runtime_stats rs ON rs.plan_id=p.plan_id ORDER BY rs.avg_duration DESC;
