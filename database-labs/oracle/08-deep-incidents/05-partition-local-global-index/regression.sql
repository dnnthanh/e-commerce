SELECT index_name,partitioned,status FROM user_indexes WHERE table_name='LAB_ORDER_HEADER';
-- Compare PSTART/PSTOP, partition maintenance duration and index usability after DROP/TRUNCATE partition on shadow table.
-- Evidence checklist:
-- Execute with statistics_level=ALL and save DBMS_XPLAN DISPLAY_CURSOR using ALLSTATS LAST +PEEKED_BINDS +PARTITION +IOSTATS.
-- Record A-Rows/E-Rows, buffer gets, physical reads, child cursor/plan hash, PGA/TEMP/UNDO or lock signals as relevant.
-- Repeat hot/tail binds and concurrent sessions; measure index maintenance/refresh cost and keep a rollback path before production acceptance.
