-- Case: find active promotions whose validity overlaps a campaign window.
-- A normal B-tree is awkward for interval-overlap predicates. GiST can index a range expression.
CREATE EXTENSION IF NOT EXISTS btree_gist;

EXPLAIN (ANALYZE, BUFFERS, SETTINGS)
SELECT id,code,stacking_group,priority,start_at,end_at
FROM promotion
WHERE status='ACTIVE'
  AND tsrange(start_at,end_at,'[)') && tsrange(TIMESTAMP '2026-08-01',TIMESTAMP '2026-08-08','[)')
ORDER BY priority DESC,id;

-- Candidate: partial GiST because only ACTIVE campaigns are queried on the checkout hot path.
CREATE INDEX IF NOT EXISTS lab_promotion_active_validity_gist
ON promotion USING gist (tsrange(start_at,end_at,'[)'))
WHERE status='ACTIVE';

-- Complementary B-tree for stacking-group/priority lookup after time filtering.
CREATE INDEX IF NOT EXISTS lab_promotion_active_group_priority
ON promotion(stacking_group,priority DESC,id)
WHERE status='ACTIVE';

ANALYZE promotion;

EXPLAIN (ANALYZE, BUFFERS, SETTINGS)
SELECT id,code,stacking_group,priority,start_at,end_at
FROM promotion
WHERE status='ACTIVE'
  AND tsrange(start_at,end_at,'[)') && tsrange(TIMESTAMP '2026-08-01',TIMESTAMP '2026-08-08','[)')
ORDER BY priority DESC,id;
