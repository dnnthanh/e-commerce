-- Reduce projection first; CLOB payload prevents a practical covering index.
SELECT /* cf_lab_projection */ id,seller_id,customer_id FROM lab_order_header WHERE status='PENDING' AND created_at>=SYSTIMESTAMP-INTERVAL '180' DAY;
CREATE INDEX ix_lab_order_status_date_proj ON lab_order_header(status,created_at,id,seller_id,customer_id);
-- Compare table block visits and DML cost; do not rebuild table solely to improve clustering factor without operational justification.
