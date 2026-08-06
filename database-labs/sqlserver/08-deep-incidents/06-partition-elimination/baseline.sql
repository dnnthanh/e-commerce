SET STATISTICS IO ON;
SELECT COUNT_BIG(*) FROM dbo.lab_order_header WHERE CONVERT(date,created_at)=CONVERT(date,SYSUTCDATETIME());
-- On a partitioned shadow table inspect Actual Partition Count/Partitions Accessed.
-- Evidence checklist:
-- Capture the Actual Execution Plan plus SET STATISTICS IO,TIME ON output. Correlate with Query Store/DMVs and Extended Events when waits/deadlocks matter.
-- Record actual-vs-estimated rows, logical reads, CPU/elapsed time, memory grant/spill warnings, wait category and plan_id.
-- Re-run hot/tail parameters and representative concurrency; compare index update cost and tempdb/memory impact before rollout.
