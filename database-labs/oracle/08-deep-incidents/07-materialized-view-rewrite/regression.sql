SELECT mview_name,refresh_mode,refresh_method,staleness,last_refresh_date FROM user_mviews WHERE mview_name='MV_LAB_SELLER_MONTH';
-- Measure refresh duration, staleness window, query rewrite use and extra log/write cost if moving to FAST REFRESH.
-- Evidence checklist:
-- Execute with statistics_level=ALL and save DBMS_XPLAN DISPLAY_CURSOR using ALLSTATS LAST +PEEKED_BINDS +PARTITION +IOSTATS.
-- Record A-Rows/E-Rows, buffer gets, physical reads, child cursor/plan hash, PGA/TEMP/UNDO or lock signals as relevant.
-- Repeat hot/tail binds and concurrent sessions; measure index maintenance/refresh cost and keep a rollback path before production acceptance.
