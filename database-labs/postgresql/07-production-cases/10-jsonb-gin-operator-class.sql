SET search_path = perf_lab, public;

EXPLAIN (ANALYZE, BUFFERS)
SELECT id,name FROM product
WHERE attributes @> '{"tier":"premium","ram":"32GB"}'::jsonb;

-- Compare default GIN vs jsonb_path_ops in separate runs; do not keep both by default.
CREATE INDEX IF NOT EXISTS lab_product_attributes_path_gin
ON product USING gin(attributes jsonb_path_ops);
ANALYZE product;

EXPLAIN (ANALYZE, BUFFERS)
SELECT id,name FROM product
WHERE attributes @> '{"tier":"premium","ram":"32GB"}'::jsonb;

-- jsonb_path_ops is compact/strong for @> containment but does not support every JSONB operator.
-- Regression queries must cover operators actually used by catalog filters before selecting the opclass.
