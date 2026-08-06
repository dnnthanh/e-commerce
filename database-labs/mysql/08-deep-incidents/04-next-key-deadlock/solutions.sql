-- Acquire exact PK rows in deterministic order and keep transaction short.
START TRANSACTION;
SELECT * FROM lab_inventory WHERE (sku_id,warehouse_id) IN ((100,1),(150,1),(200,1)) ORDER BY sku_id,warehouse_id FOR UPDATE;
-- perform updates; COMMIT before external network calls.
-- Evaluate READ COMMITTED only with application-level phantom semantics understood; do not change isolation solely to hide deadlocks.
