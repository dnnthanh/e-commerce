SELECT seller_id,SUM(gross),SUM(commission) FROM dbo.lab_settlement WHERE business_date>=DATEADD(day,-365,CAST(SYSUTCDATETIME() AS date)) GROUP BY seller_id;
-- Capture actual plan and note row mode vs batch mode.
-- Evidence checklist:
-- Capture the Actual Execution Plan plus SET STATISTICS IO,TIME ON output. Correlate with Query Store/DMVs and Extended Events when waits/deadlocks matter.
-- Record actual-vs-estimated rows, logical reads, CPU/elapsed time, memory grant/spill warnings, wait category and plan_id.
-- Re-run hot/tail parameters and representative concurrency; compare index update cost and tempdb/memory impact before rollout.
