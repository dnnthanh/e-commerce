SELECT seller_id,COUNT(*) c FROM lab_order_header GROUP BY seller_id ORDER BY c DESC FETCH FIRST 20 ROWS ONLY;
SELECT sql_id,child_number,plan_hash_value,executions,buffer_gets,rows_processed,is_bind_sensitive,is_bind_aware FROM v$sql WHERE sql_text LIKE '%acs_lab%';
-- Evidence checklist:
-- Execute with statistics_level=ALL and save DBMS_XPLAN DISPLAY_CURSOR using ALLSTATS LAST +PEEKED_BINDS +PARTITION +IOSTATS.
-- Record A-Rows/E-Rows, buffer gets, physical reads, child cursor/plan hash, PGA/TEMP/UNDO or lock signals as relevant.
-- Repeat hot/tail binds and concurrent sessions; measure index maintenance/refresh cost and keep a rollback path before production acceptance.
