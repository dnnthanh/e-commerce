SELECT * FROM sys.dm_db_file_space_usage;
-- Capture version store MB/minute, longest snapshot duration, tempdb read/write latency, and OLTP p95 before/during/after the long snapshot.
-- Evidence checklist:
-- Capture the Actual Execution Plan plus SET STATISTICS IO,TIME ON output. Correlate with Query Store/DMVs and Extended Events when waits/deadlocks matter.
-- Record actual-vs-estimated rows, logical reads, CPU/elapsed time, memory grant/spill warnings, wait category and plan_id.
-- Re-run hot/tail parameters and representative concurrency; compare index update cost and tempdb/memory impact before rollout.
