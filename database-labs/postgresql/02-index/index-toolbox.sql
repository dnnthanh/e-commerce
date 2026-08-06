-- PostgreSQL index toolbox. Apply ONE section at a time, capture a plan, then DROP it before the next comparison.

-- catalog_db: covering B-tree. INCLUDE can enable index-only scans when visibility map permits.
CREATE INDEX CONCURRENTLY idx_product_published_category_recent
ON product(category_id,created_at DESC,id DESC)
INCLUDE (seller_id,name)
WHERE status='PUBLISHED';

-- catalog_db: functional index. Query expression must match lower(name), not name ILIKE with arbitrary leading wildcard.
CREATE INDEX CONCURRENTLY idx_product_lower_name
ON product(lower(name) text_pattern_ops)
WHERE status='PUBLISHED';

-- catalog_db: JSONB containment. jsonb_path_ops is compact/focused for @> containment.
CREATE INDEX CONCURRENTLY idx_product_attribute_value_gin
ON product_attribute_value USING gin(value_json jsonb_path_ops);

-- catalog_db: join support from product -> sku and product -> attributes.
CREATE INDEX CONCURRENTLY idx_sku_product_active
ON sku(product_id,id)
INCLUDE (variant_name)
WHERE active=true;
CREATE INDEX CONCURRENTLY idx_product_attribute_product_definition
ON product_attribute_value(product_id,attribute_definition_id);

-- catalog_db: queue history is huge, live PENDING set is tiny: partial index wins on size/write cost.
CREATE INDEX CONCURRENTLY idx_outbox_pending_created
ON outbox_event(created_at,id)
INCLUDE (event_id,aggregate_id,event_type)
WHERE status = 'PENDING';

-- inventory_db: point/range history for one SKU/warehouse.
CREATE INDEX CONCURRENTLY idx_inventory_ledger_sku_warehouse_time
ON inventory_ledger(sku_id,warehouse_id,created_at DESC)
INCLUDE (delta,reason,reference_key);

-- inventory_db: append-heavy time history. BRIN is tiny and useful for large naturally time-correlated ranges.
CREATE INDEX CONCURRENTLY idx_inventory_ledger_created_brin
ON inventory_ledger USING brin(created_at) WITH (pages_per_range=64);

-- payment_db: keyset pagination + common customer history projection.
CREATE INDEX CONCURRENTLY idx_payment_user_created_id
ON payment(user_id,created_at DESC,id DESC)
INCLUDE (payment_key,amount,status,provider);
