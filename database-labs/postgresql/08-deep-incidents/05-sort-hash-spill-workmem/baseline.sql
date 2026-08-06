SET work_mem='1MB';
SET max_parallel_workers_per_gather=4;
EXPLAIN (ANALYZE,BUFFERS,SETTINGS)
SELECT seller_id,business_date,sum(gross) gross,sum(commission) commission,count(*) rows
FROM lab_settlement WHERE business_date>=current_date-365 GROUP BY seller_id,business_date ORDER BY gross DESC LIMIT 5000;
-- Inspect Sort Method, Disk, Hash Batches, temp read/write and worker count.
