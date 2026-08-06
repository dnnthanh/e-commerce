SET search_path = perf_lab, public;
SELECT correlation FROM pg_stats WHERE schemaname='perf_lab' AND tablename='inventory_ledger' AND attname='created_at';

EXPLAIN (ANALYZE, BUFFERS)
SELECT warehouse_id,sum(delta)
FROM inventory_ledger
WHERE created_at>=now()-interval '30 days'
GROUP BY warehouse_id;

CREATE INDEX IF NOT EXISTS lab_inventory_ledger_created_brin
ON inventory_ledger USING brin(created_at) WITH (pages_per_range=64);

EXPLAIN (ANALYZE, BUFFERS)
SELECT warehouse_id,sum(delta)
FROM inventory_ledger
WHERE created_at>=now()-interval '30 days'
GROUP BY warehouse_id;

-- Compare index size with a temporary B-tree candidate before choosing.
-- BRIN wins when physical order correlates with time and ranges are broad; it is not a point-lookup index.
