SELECT object_name(object_id) table_name,index_id,state_description,total_rows,deleted_rows,size_in_bytes FROM sys.dm_db_column_store_row_group_physical_stats WHERE object_id=OBJECT_ID('dbo.lab_settlement');
-- Compare analytics elapsed/logical reads and insert/update throughput with/without NCCI.
-- Evidence checklist:
-- Capture the Actual Execution Plan plus SET STATISTICS IO,TIME ON output. Correlate with Query Store/DMVs and Extended Events when waits/deadlocks matter.
-- Record actual-vs-estimated rows, logical reads, CPU/elapsed time, memory grant/spill warnings, wait category and plan_id.
-- Re-run hot/tail parameters and representative concurrency; compare index update cost and tempdb/memory impact before rollout.
