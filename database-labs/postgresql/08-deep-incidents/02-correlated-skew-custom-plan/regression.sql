SELECT seller_id,status,count(*) FROM lab_order_header GROUP BY seller_id,status ORDER BY count(*) DESC LIMIT 30;
SELECT name,generic_plans,custom_plans FROM pg_prepared_statements WHERE name='order_probe';
-- Compare E-Rows/A-Rows and buffers before/after extended stats for hot+tail combinations.
DEALLOCATE order_probe;
