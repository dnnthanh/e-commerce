-- Operational fix is usually transaction hygiene, not VACUUM FULL first.
-- 1) terminate/shorten stale reporting transactions; 2) tune autovacuum on the hot table; 3) vacuum normally; 4) only then assess rewrite/repack downtime.
ALTER TABLE lab_inventory SET (autovacuum_vacuum_scale_factor=0.02,autovacuum_analyze_scale_factor=0.01,autovacuum_vacuum_cost_limit=3000);
VACUUM (ANALYZE) lab_inventory;
CREATE INDEX IF NOT EXISTS ix_lab_inventory_updated ON lab_inventory(updated_at,sku_id) INCLUDE(reserved,on_hand);
EXPLAIN (ANALYZE,BUFFERS) SELECT sku_id,reserved,on_hand FROM lab_inventory WHERE updated_at>=now()-interval '5 minutes' ORDER BY updated_at DESC LIMIT 1000;
