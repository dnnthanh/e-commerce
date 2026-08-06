SELECT name,value FROM v$pgastat WHERE name IN ('aggregate PGA target parameter','total PGA allocated','over allocation count');
SELECT tablespace_name,used_blocks,free_blocks FROM v$sort_segment;
-- Run at realistic concurrency and record onepass/multipass rate plus TEMP latency.
-- Evidence checklist:
-- Execute with statistics_level=ALL and save DBMS_XPLAN DISPLAY_CURSOR using ALLSTATS LAST +PEEKED_BINDS +PARTITION +IOSTATS.
-- Record A-Rows/E-Rows, buffer gets, physical reads, child cursor/plan hash, PGA/TEMP/UNDO or lock signals as relevant.
-- Repeat hot/tail binds and concurrent sessions; measure index maintenance/refresh cost and keep a rollback path before production acceptance.
