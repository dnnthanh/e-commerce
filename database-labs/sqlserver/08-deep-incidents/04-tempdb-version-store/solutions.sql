-- Correct the application/reporting transaction scope first. Size tempdb with multiple equally-sized data files and preallocation appropriate to workload.
-- Do not disable RCSI blindly: compare blocking cost against version-store cost.
COMMIT;
-- Evidence checklist:
-- Capture the Actual Execution Plan plus SET STATISTICS IO,TIME ON output. Correlate with Query Store/DMVs and Extended Events when waits/deadlocks matter.
-- Record actual-vs-estimated rows, logical reads, CPU/elapsed time, memory grant/spill warnings, wait category and plan_id.
-- Re-run hot/tail parameters and representative concurrency; compare index update cost and tempdb/memory impact before rollout.
