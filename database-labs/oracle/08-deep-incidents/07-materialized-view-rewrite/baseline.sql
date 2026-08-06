SELECT /* mv_lab */ seller_id,TRUNC(created_at,'MM') month_key,COUNT(*) orders FROM lab_order_header GROUP BY seller_id,TRUNC(created_at,'MM');
-- Record elapsed/buffer gets for repeated dashboard executions.
-- Evidence checklist:
-- Execute with statistics_level=ALL and save DBMS_XPLAN DISPLAY_CURSOR using ALLSTATS LAST +PEEKED_BINDS +PARTITION +IOSTATS.
-- Record A-Rows/E-Rows, buffer gets, physical reads, child cursor/plan hash, PGA/TEMP/UNDO or lock signals as relevant.
-- Repeat hot/tail binds and concurrent sessions; measure index maintenance/refresh cost and keep a rollback path before production acceptance.
