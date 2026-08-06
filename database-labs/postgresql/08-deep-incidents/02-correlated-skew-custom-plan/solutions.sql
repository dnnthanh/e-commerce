CREATE STATISTICS IF NOT EXISTS st_lab_seller_status (dependencies,mcv) ON seller_id,status FROM lab_order_header;
ALTER TABLE lab_order_header ALTER COLUMN seller_id SET STATISTICS 1000;
ALTER TABLE lab_order_header ALTER COLUMN status SET STATISTICS 1000;
ANALYZE lab_order_header;
SELECT statistics_name,kinds FROM pg_stats_ext WHERE tablename='lab_order_header';
-- Prefer corrected statistics over permanent force_custom_plan unless measured parameter skew still requires it.
