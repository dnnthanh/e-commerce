CREATE INDEX ix_lab_line_order_amount ON lab_order_line(order_id,amount);
BEGIN DBMS_STATS.GATHER_TABLE_STATS(USER,'LAB_ORDER_LINE',cascade=>TRUE); END;/
-- Compare optimizer statistics/query shape first. PGA_AGGREGATE_TARGET changes are system-wide capacity decisions, not per-query reflexes.
-- Evidence checklist:
-- Execute with statistics_level=ALL and save DBMS_XPLAN DISPLAY_CURSOR using ALLSTATS LAST +PEEKED_BINDS +PARTITION +IOSTATS.
-- Record A-Rows/E-Rows, buffer gets, physical reads, child cursor/plan hash, PGA/TEMP/UNDO or lock signals as relevant.
-- Repeat hot/tail binds and concurrent sessions; measure index maintenance/refresh cost and keep a rollback path before production acceptance.
