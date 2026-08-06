SELECT COUNT_BIG(*) FROM dbo.lab_order_header WHERE created_at>=CONVERT(date,SYSUTCDATETIME()) AND created_at<DATEADD(day,1,CONVERT(date,SYSUTCDATETIME()));
-- Build aligned local indexes on the partition scheme before testing SWITCH-based retention.
-- Evidence checklist:
-- Capture the Actual Execution Plan plus SET STATISTICS IO,TIME ON output. Correlate with Query Store/DMVs and Extended Events when waits/deadlocks matter.
-- Record actual-vs-estimated rows, logical reads, CPU/elapsed time, memory grant/spill warnings, wait category and plan_id.
-- Re-run hot/tail parameters and representative concurrency; compare index update cost and tempdb/memory impact before rollout.
