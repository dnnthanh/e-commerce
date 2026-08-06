SET search_path = perf_lab, public;

-- INCIDENT: report is fast on dev but spills temp files in production.
-- Keep session setting scoped to this experiment, never globally tune from one query.
BEGIN;
SET LOCAL work_mem='2MB';
EXPLAIN (ANALYZE, BUFFERS, SETTINGS)
SELECT seller_id,product_id,sum(quantity*amount) gross
FROM invoice_line il
JOIN invoice i ON i.id=il.invoice_id
GROUP BY seller_id,product_id
ORDER BY gross DESC
LIMIT 5000;

-- Look for Sort Method: external merge, Disk, temp read/written and hash batches.
-- Candidate A: reduce input rows with a business time/status predicate.
EXPLAIN (ANALYZE, BUFFERS, SETTINGS)
SELECT i.seller_id,il.product_id,sum(il.quantity*il.amount) gross
FROM invoice_line il
JOIN invoice i ON i.id=il.invoice_id
WHERE i.status='SIGNED' AND i.invoice_date>=current_date-30
GROUP BY i.seller_id,il.product_id
ORDER BY gross DESC
LIMIT 5000;

-- Candidate B: test a larger work_mem only for the reporting transaction/session.
SET LOCAL work_mem='64MB';
-- rerun Candidate A and compare total memory concurrency risk: 64MB is per eligible sort/hash node, not per query.
ROLLBACK;
