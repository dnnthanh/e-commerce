ALTER SESSION SET statistics_level=ALL;
SELECT /*+ gather_plan_statistics */
       settlement_no,seller_id,period_start,period_end,status,payable_amount
FROM settlement
WHERE seller_id=:seller_id
  AND period_start>=:from_period
ORDER BY period_start DESC
FETCH FIRST 100 ROWS ONLY;

SELECT * FROM TABLE(DBMS_XPLAN.DISPLAY_CURSOR(NULL,NULL,'ALLSTATS LAST +PEEKED_BINDS +IOSTATS'));
-- Repeat with one dominant seller and one long-tail seller. Do not accept an index/hint tested on only one distribution.
