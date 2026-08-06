-- Compare bounded session-level experiments, not global work_mem first.
SET LOCAL work_mem='64MB';
EXPLAIN (ANALYZE,BUFFERS,SETTINGS) SELECT seller_id,business_date,sum(gross) gross,sum(commission) commission FROM lab_settlement WHERE business_date>=current_date-365 GROUP BY seller_id,business_date ORDER BY gross DESC LIMIT 5000;
CREATE INDEX IF NOT EXISTS ix_lab_settlement_date_seller ON lab_settlement(business_date,seller_id) INCLUDE(gross,commission);
-- If this is a recurring dashboard, test pre-aggregation/materialized view rather than paying full grouping every request.
