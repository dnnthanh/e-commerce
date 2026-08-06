-- Session A
START TRANSACTION; SELECT * FROM lab_inventory WHERE sku_id BETWEEN 100 AND 200 AND warehouse_id=1 FOR UPDATE;
-- Session B: acquire overlapping range in reverse order before A commits.
-- START TRANSACTION; SELECT * FROM lab_inventory WHERE sku_id BETWEEN 150 AND 250 AND warehouse_id=1 ORDER BY sku_id DESC FOR UPDATE;
SHOW ENGINE INNODB STATUS;
SELECT * FROM performance_schema.data_lock_waits;
