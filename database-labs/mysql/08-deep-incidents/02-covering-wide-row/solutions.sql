CREATE INDEX ix_line_order_projection ON lab_order_line(order_id,id,sku_id,qty,amount);
-- Do not INCLUDE LONGTEXT payloads in a covering strategy; fetch details only for selected ids.
SELECT id,sku_id,qty,amount FROM lab_order_line WHERE order_id BETWEEN 100000 AND 120000;
SELECT id,description FROM lab_order_line WHERE id IN (/* small selected id list */ 1,2,3);
