SET search_path = perf_lab, public;

-- INCIDENT: filter is indexed/cheap, selecting one narrow column is fast, SELECT * is dramatically slower.
-- This reproduces large TOASTed text/json columns and network/serialization cost.
EXPLAIN (ANALYZE, BUFFERS)
SELECT invoice_no
FROM invoice
WHERE seller_id=42
ORDER BY invoice_date DESC
LIMIT 200;

EXPLAIN (ANALYZE, BUFFERS)
SELECT *
FROM invoice
WHERE seller_id=42
ORDER BY invoice_date DESC
LIMIT 200;

-- Measure width/TOAST candidates instead of guessing.
SELECT avg(pg_column_size(i)) AS avg_row_bytes,
       max(pg_column_size(attachment_text)) AS max_attachment_bytes,
       percentile_cont(0.99) WITHIN GROUP (ORDER BY pg_column_size(attachment_text)) AS p99_attachment_bytes
FROM invoice i;

-- Inspect relation and TOAST sizes.
SELECT pg_size_pretty(pg_relation_size('invoice')) heap,
       pg_size_pretty(pg_total_relation_size('invoice')) total_with_toast_and_indexes;

-- Production decision candidates:
-- 1) list/search projection excludes payload/attachment_text;
-- 2) detail endpoint fetches the large fields only when requested;
-- 3) move immutable large attachment body to object storage, DB keeps metadata/key;
-- 4) vertical split only if ownership/lifecycle justify the extra join.
-- Do NOT "fix" this by indexing attachment_text.
