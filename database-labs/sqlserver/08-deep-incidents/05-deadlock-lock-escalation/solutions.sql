-- Canonical access order: always parent then line; reduce batch size; ensure selective supporting indexes.
CREATE INDEX IX_lab_line_order ON dbo.lab_order_line(order_id);
-- Prefer retry for true deadlock victims after fixing ordering; NOLOCK is not a correctness-safe deadlock solution.
-- Evidence checklist:
-- Capture the Actual Execution Plan plus SET STATISTICS IO,TIME ON output. Correlate with Query Store/DMVs and Extended Events when waits/deadlocks matter.
-- Record actual-vs-estimated rows, logical reads, CPU/elapsed time, memory grant/spill warnings, wait category and plan_id.
-- Re-run hot/tail parameters and representative concurrency; compare index update cost and tempdb/memory impact before rollout.
