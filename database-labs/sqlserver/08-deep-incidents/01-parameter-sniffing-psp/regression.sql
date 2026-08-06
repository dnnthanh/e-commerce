SELECT TOP 20 seller_id,COUNT_BIG(*) c FROM dbo.lab_order_header GROUP BY seller_id ORDER BY c DESC;
-- Compare logical reads, CPU, duration and compile count for hot/tail parameters. Use Query Store runtime stats intervals, not one execution.
-- Evidence checklist:
-- Capture the Actual Execution Plan plus SET STATISTICS IO,TIME ON output. Correlate with Query Store/DMVs and Extended Events when waits/deadlocks matter.
-- Record actual-vs-estimated rows, logical reads, CPU/elapsed time, memory grant/spill warnings, wait category and plan_id.
-- Re-run hot/tail parameters and representative concurrency; compare index update cost and tempdb/memory impact before rollout.
