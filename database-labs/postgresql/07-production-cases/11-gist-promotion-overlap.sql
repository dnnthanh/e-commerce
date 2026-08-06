SET search_path = perf_lab, public;
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- INCIDENT: campaign validation asks whether a seller/SKU already has an overlapping window.
EXPLAIN (ANALYZE, BUFFERS)
SELECT id
FROM promotion_window
WHERE seller_id=42 AND sku_id=10042
  AND tstzrange(valid_from,valid_to,'[)') && tstzrange(now(),now()+interval '7 days','[)');

CREATE INDEX IF NOT EXISTS lab_promotion_overlap_gist
ON promotion_window USING gist(
  seller_id,
  sku_id,
  tstzrange(valid_from,valid_to,'[)')
);

EXPLAIN (ANALYZE, BUFFERS)
SELECT id
FROM promotion_window
WHERE seller_id=42 AND sku_id=10042
  AND tstzrange(valid_from,valid_to,'[)') && tstzrange(now(),now()+interval '7 days','[)');

-- If business invariant is "no overlaps", evaluate an EXCLUDE constraint instead of only speeding up a check-then-insert race.
