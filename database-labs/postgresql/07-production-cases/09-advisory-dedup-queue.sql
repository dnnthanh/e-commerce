-- INCIDENT: multiple source-table changes enqueue recalculation for the same product concurrently.
CREATE TABLE IF NOT EXISTS perf_lab.recalculation_queue(
  id bigserial PRIMARY KEY,
  product_code text NOT NULL,
  source_type text NOT NULL,
  changed_at timestamptz NOT NULL DEFAULT clock_timestamp(),
  active boolean NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS lab_one_active_recalc_per_product
ON perf_lab.recalculation_queue(product_code) WHERE active;

CREATE OR REPLACE FUNCTION perf_lab.enqueue_recalc(p_product text,p_source text)
RETURNS void LANGUAGE plpgsql AS $$
BEGIN
  -- Serialize only the same logical product within this transaction.
  PERFORM pg_advisory_xact_lock(hashtextextended(p_product,0));
  UPDATE perf_lab.recalculation_queue SET active=false
   WHERE product_code=p_product AND active;
  INSERT INTO perf_lab.recalculation_queue(product_code,source_type,active)
  VALUES(p_product,p_source,true);
END $$;

-- Run concurrently in several sessions for identical and different product codes.
SELECT perf_lab.enqueue_recalc('SKU-100','PRICE');
SELECT perf_lab.enqueue_recalc('SKU-100','INVENTORY');
SELECT perf_lab.enqueue_recalc('SKU-200','PRICE');

SELECT product_code,count(*) FILTER (WHERE active) active_count
FROM perf_lab.recalculation_queue GROUP BY product_code;
-- Expected active_count <= 1. Discuss hash collision risk and why the unique partial index remains the invariant backstop.
