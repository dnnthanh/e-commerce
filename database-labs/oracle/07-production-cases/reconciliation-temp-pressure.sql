ALTER SESSION SET statistics_level=ALL;
SELECT /*+ gather_plan_statistics */ s.seller_id,
       SUM(l.gross_amount-l.fee_amount) ledger_net,
       SUM(s.payable_amount) settlement_net
FROM settlement s
JOIN settlement_ledger l ON l.settlement_no=s.settlement_no
WHERE s.period_start>=:from_period AND s.period_start<:to_period
GROUP BY s.seller_id
HAVING ABS(SUM(l.gross_amount-l.fee_amount)-SUM(s.payable_amount))>1;
SELECT * FROM TABLE(DBMS_XPLAN.DISPLAY_CURSOR(NULL,NULL,'ALLSTATS LAST +IOSTATS +MEMSTATS'));
-- Diagnose A-Rows/E-Rows, hash workarea and TEMP before changing PGA. Reduce rows and fix estimates first.
