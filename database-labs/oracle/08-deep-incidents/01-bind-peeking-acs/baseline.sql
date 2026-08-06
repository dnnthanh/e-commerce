ALTER SESSION SET statistics_level=ALL;
VAR seller_id NUMBER; EXEC :seller_id:=42;
SELECT /* acs_lab */ id,status,created_at FROM lab_order_header WHERE seller_id=:seller_id AND created_at>=SYSTIMESTAMP-INTERVAL '90' DAY ORDER BY created_at DESC;
EXEC :seller_id:=999;
SELECT /* acs_lab */ id,status,created_at FROM lab_order_header WHERE seller_id=:seller_id AND created_at>=SYSTIMESTAMP-INTERVAL '90' DAY ORDER BY created_at DESC;
SELECT * FROM TABLE(DBMS_XPLAN.DISPLAY_CURSOR(NULL,NULL,'ALLSTATS LAST +PEEKED_BINDS +IOSTATS'));
