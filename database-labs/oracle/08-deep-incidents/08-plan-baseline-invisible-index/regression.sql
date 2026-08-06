SELECT index_name,visibility,status FROM user_indexes WHERE table_name='LAB_ORDER_HEADER';
SELECT sql_id,plan_hash_value,executions,buffer_gets,elapsed_time FROM v$sql WHERE sql_text LIKE '%spm_lab%';
-- Compare hot/tail sellers and DML cost before making candidate visible globally.
-- Evidence checklist:
-- Execute with statistics_level=ALL and save DBMS_XPLAN DISPLAY_CURSOR using ALLSTATS LAST +PEEKED_BINDS +PARTITION +IOSTATS.
-- Record A-Rows/E-Rows, buffer gets, physical reads, child cursor/plan hash, PGA/TEMP/UNDO or lock signals as relevant.
-- Repeat hot/tail binds and concurrent sessions; measure index maintenance/refresh cost and keep a rollback path before production acceptance.
