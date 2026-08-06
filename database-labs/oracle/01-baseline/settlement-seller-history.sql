ALTER SESSION SET statistics_level = ALL;
SELECT settlement_no, seller_id, payable_amount, status, updated_at
FROM settlement
WHERE seller_id = 10001
ORDER BY updated_at DESC
FETCH FIRST 100 ROWS ONLY;
SELECT * FROM TABLE(DBMS_XPLAN.DISPLAY_CURSOR(NULL,NULL,'ALLSTATS LAST +BUFFERS +PREDICATE'));
