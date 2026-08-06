CREATE INDEX IX_lab_order_status ON dbo.lab_order_header(status,created_at DESC);
SET STATISTICS IO,TIME ON;
SELECT id,seller_id,customer_id,status,created_at,payload FROM dbo.lab_order_header WHERE status='PENDING' ORDER BY created_at DESC;
-- Inspect Key Lookup execution count and logical reads.
-- Evidence checklist:
-- Capture the Actual Execution Plan plus SET STATISTICS IO,TIME ON output. Correlate with Query Store/DMVs and Extended Events when waits/deadlocks matter.
-- Record actual-vs-estimated rows, logical reads, CPU/elapsed time, memory grant/spill warnings, wait category and plan_id.
-- Re-run hot/tail parameters and representative concurrency; compare index update cost and tempdb/memory impact before rollout.
