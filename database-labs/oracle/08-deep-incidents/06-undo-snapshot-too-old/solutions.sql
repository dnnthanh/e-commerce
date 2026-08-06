-- Shorten/split reporting transactions or use appropriate replicas/MVs; size UNDO from measured longest query + write rate.
SELECT tablespace_name,status,contents FROM dba_tablespaces WHERE contents='UNDO';
-- Do not merely set a large UNDO_RETENTION if tablespace cannot retain the required versions.
-- Evidence checklist:
-- Execute with statistics_level=ALL and save DBMS_XPLAN DISPLAY_CURSOR using ALLSTATS LAST +PEEKED_BINDS +PARTITION +IOSTATS.
-- Record A-Rows/E-Rows, buffer gets, physical reads, child cursor/plan hash, PGA/TEMP/UNDO or lock signals as relevant.
-- Repeat hot/tail binds and concurrent sessions; measure index maintenance/refresh cost and keep a rollback path before production acceptance.
