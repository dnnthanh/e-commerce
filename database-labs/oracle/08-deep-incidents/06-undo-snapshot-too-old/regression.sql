SELECT MAX(maxquerylen) max_query_seconds,SUM(ssolderrcnt) ora1555_count,MAX(tuned_undoretention) tuned_retention FROM v$undostat WHERE begin_time>SYSTIMESTAMP-INTERVAL '1' DAY;
-- Evidence checklist:
-- Execute with statistics_level=ALL and save DBMS_XPLAN DISPLAY_CURSOR using ALLSTATS LAST +PEEKED_BINDS +PARTITION +IOSTATS.
-- Record A-Rows/E-Rows, buffer gets, physical reads, child cursor/plan hash, PGA/TEMP/UNDO or lock signals as relevant.
-- Repeat hot/tail binds and concurrent sessions; measure index maintenance/refresh cost and keep a rollback path before production acceptance.
