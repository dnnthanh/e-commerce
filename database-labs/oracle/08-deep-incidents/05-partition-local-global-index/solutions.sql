SELECT /* part_lab_sarg */ COUNT(*) FROM lab_order_header WHERE created_at>=TRUNC(SYSTIMESTAMP) AND created_at<TRUNC(SYSTIMESTAMP)+INTERVAL '1' DAY;
-- Prefer LOCAL indexes for partition-local access/retention when workload permits; test global index maintenance with UPDATE GLOBAL INDEXES separately.
-- Evidence checklist:
-- Execute with statistics_level=ALL and save DBMS_XPLAN DISPLAY_CURSOR using ALLSTATS LAST +PEEKED_BINDS +PARTITION +IOSTATS.
-- Record A-Rows/E-Rows, buffer gets, physical reads, child cursor/plan hash, PGA/TEMP/UNDO or lock signals as relevant.
-- Repeat hot/tail binds and concurrent sessions; measure index maintenance/refresh cost and keep a rollback path before production acceptance.
