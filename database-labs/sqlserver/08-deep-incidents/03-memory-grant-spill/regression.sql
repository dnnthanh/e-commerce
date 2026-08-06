SELECT qs.plan_handle,qs.execution_count,qs.total_elapsed_time,qs.total_logical_reads FROM sys.dm_exec_query_stats qs CROSS APPLY sys.dm_exec_sql_text(qs.sql_handle) t WHERE t.text LIKE '%lab_order_line%';
-- Execute repeatedly and record granted/used memory, spills and concurrency queueing.
-- Evidence checklist:
-- Capture the Actual Execution Plan plus SET STATISTICS IO,TIME ON output. Correlate with Query Store/DMVs and Extended Events when waits/deadlocks matter.
-- Record actual-vs-estimated rows, logical reads, CPU/elapsed time, memory grant/spill warnings, wait category and plan_id.
-- Re-run hot/tail parameters and representative concurrency; compare index update cost and tempdb/memory impact before rollout.
