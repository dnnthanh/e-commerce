SET search_path = perf_lab, public;

-- INCIDENT: finance report mất 30–40s sau khi thêm nhiều enrichment join.
-- STEP 1 - baseline: intentionally materialize a broad candidate set and join before reducing rows.
EXPLAIN (ANALYZE, BUFFERS, WAL, SETTINGS)
WITH candidate_lines AS MATERIALIZED (
  SELECT il.*
  FROM invoice_line il
  WHERE il.brand_code IN ('BR001','BR002','BR003','BR004','BR005','BR006','BR007')
), signed_invoice AS MATERIALIZED (
  SELECT id, invoice_no, seller_id, invoice_date
  FROM invoice
  WHERE status='SIGNED'
    AND invoice_date >= current_date - 365
)
SELECT i.seller_id,
       count(*) line_count,
       sum(c.quantity * c.amount) gross_amount
FROM candidate_lines c
JOIN signed_invoice i ON i.id=c.invoice_id
JOIN product p ON p.id=c.product_id
WHERE p.status='PUBLISHED'
GROUP BY i.seller_id
ORDER BY gross_amount DESC
LIMIT 50;

-- READ THE PLAN:
-- * candidate_lines actual rows vs final rows.
-- * loops on product lookup / join nodes.
-- * hash table memory/batches and temp blocks.
-- * whether MATERIALIZED prevented predicate pushdown.
-- * estimate vs actual for brand_code and status/date combination.

-- HYPOTHESIS A: reduce invoice keys before touching the large line table.
EXPLAIN (ANALYZE, BUFFERS, WAL, SETTINGS)
WITH valid_invoice AS (
  SELECT id, seller_id
  FROM invoice
  WHERE status='SIGNED'
    AND invoice_date >= current_date - 365
)
SELECT i.seller_id,
       count(*) line_count,
       sum(il.quantity * il.amount) gross_amount
FROM valid_invoice i
JOIN invoice_line il ON il.invoice_id=i.id
JOIN product p ON p.id=il.product_id AND p.status='PUBLISHED'
WHERE il.brand_code = ANY (ARRAY['BR001','BR002','BR003','BR004','BR005','BR006','BR007'])
GROUP BY i.seller_id
ORDER BY gross_amount DESC
LIMIT 50;

-- HYPOTHESIS B: only after measuring, test indexes separately.
-- CREATE INDEX CONCURRENTLY lab_invoice_signed_date_id
--   ON invoice(invoice_date,id) INCLUDE (seller_id)
--   WHERE status='SIGNED';
-- CREATE INDEX CONCURRENTLY lab_invoice_line_brand_invoice
--   ON invoice_line(brand_code,invoice_id) INCLUDE(product_id,quantity,amount);
-- ANALYZE invoice; ANALYZE invoice_line;

-- REGRESSION: repeat for 7 brands, 100 brands, 30-day range, 365-day range.
-- A bitmap/seq scan can become correct when the requested share becomes large.
