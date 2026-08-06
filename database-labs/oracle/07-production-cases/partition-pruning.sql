ALTER SESSION SET statistics_level=ALL;
-- Compare function-wrapped partition key with a sargable range.
SELECT /*+ gather_plan_statistics */ settlement_no,seller_id,status,payable_amount
FROM settlement
WHERE TRUNC(period_start)=:business_day;
SELECT * FROM TABLE(DBMS_XPLAN.DISPLAY_CURSOR(NULL,NULL,'ALLSTATS LAST +PARTITION +IOSTATS'));

SELECT /*+ gather_plan_statistics */ settlement_no,seller_id,status,payable_amount
FROM settlement
WHERE period_start>=:business_day AND period_start<:business_day+1;
SELECT * FROM TABLE(DBMS_XPLAN.DISPLAY_CURSOR(NULL,NULL,'ALLSTATS LAST +PARTITION +IOSTATS'));
-- Inspect PSTART/PSTOP and actual buffers. The second form should allow pruning when partitioned by period_start.
