SELECT query_id,plan_id,is_forced_plan,force_failure_count,last_force_failure_reason_desc FROM sys.query_store_plan WHERE is_forced_plan=1 OR force_failure_count>0;
-- Compare runtime intervals before/after deploy and after statistics refresh.
-- Evidence checklist:
-- Capture the Actual Execution Plan plus SET STATISTICS IO,TIME ON output. Correlate with Query Store/DMVs and Extended Events when waits/deadlocks matter.
-- Record actual-vs-estimated rows, logical reads, CPU/elapsed time, memory grant/spill warnings, wait category and plan_id.
-- Re-run hot/tail parameters and representative concurrency; compare index update cost and tempdb/memory impact before rollout.
