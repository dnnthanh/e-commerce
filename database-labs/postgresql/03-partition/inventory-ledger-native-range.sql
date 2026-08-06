-- Database: inventory_db
-- Do NOT partition the application table first. Build a lab copy and compare identical queries.
DROP TABLE IF EXISTS inventory_ledger_lab CASCADE;
CREATE TABLE inventory_ledger_lab (
  id bigint NOT NULL,
  sku_id bigint NOT NULL,
  warehouse_id bigint NOT NULL,
  delta bigint NOT NULL,
  reason varchar(64) NOT NULL,
  reference_key varchar(128) NOT NULL,
  created_at timestamp NOT NULL
) PARTITION BY RANGE(created_at);

DO $$
DECLARE
  month_start date:=date_trunc('month',now()-interval '18 month')::date;
  month_end date;
BEGIN
  WHILE month_start<date_trunc('month',now()+interval '3 month')::date LOOP
    month_end:=(month_start+interval '1 month')::date;
    EXECUTE format(
      'CREATE TABLE inventory_ledger_lab_%s PARTITION OF inventory_ledger_lab FOR VALUES FROM (%L) TO (%L)',
      to_char(month_start,'YYYYMM'),month_start,month_end);
    month_start:=month_end;
  END LOOP;
END $$;
CREATE TABLE inventory_ledger_lab_default PARTITION OF inventory_ledger_lab DEFAULT;

INSERT INTO inventory_ledger_lab
SELECT id,sku_id,warehouse_id,delta,reason,reference_key,created_at FROM inventory_ledger;

-- Local-equivalent child indexes are created on each partition by a partitioned index.
CREATE INDEX idx_inventory_ledger_lab_sku_time ON inventory_ledger_lab(sku_id,created_at DESC);
ANALYZE inventory_ledger_lab;
