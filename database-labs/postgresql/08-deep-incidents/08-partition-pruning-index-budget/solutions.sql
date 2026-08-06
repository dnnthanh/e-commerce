-- Sargable predicate first.
EXPLAIN (ANALYZE,BUFFERS) SELECT seller_id,sum(gross) FROM lab_settlement WHERE business_date>=date_trunc('month',current_date)::date AND business_date<(date_trunc('month',current_date)+interval '1 month')::date GROUP BY seller_id;
CREATE INDEX IF NOT EXISTS brin_lab_settlement_date ON lab_settlement USING brin(business_date) WITH (pages_per_range=64);
CREATE INDEX IF NOT EXISTS ix_lab_settlement_seller_date ON lab_settlement(seller_id,business_date DESC) INCLUDE(gross,commission);
-- For a true partition lab, create a partitioned shadow table and compare Subplans Removed/maintenance before migrating production data.
