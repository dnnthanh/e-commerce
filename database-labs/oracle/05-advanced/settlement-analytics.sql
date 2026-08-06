ALTER SESSION SET statistics_level=ALL;

SELECT /*+ gather_plan_statistics */
  seller_id,settlement_no,period_start,payable_amount,
  ROW_NUMBER() OVER(PARTITION BY seller_id ORDER BY period_start DESC,settlement_no DESC) AS recent_rank,
  SUM(payable_amount) OVER(PARTITION BY seller_id ORDER BY period_start ROWS BETWEEN 5 PRECEDING AND CURRENT ROW) AS rolling_6_period_payable,
  SUM(CASE WHEN status='PAID' THEN payable_amount ELSE 0 END) OVER(PARTITION BY seller_id) AS paid_lifetime
FROM settlement
WHERE period_start>=ADD_MONTHS(TRUNC(SYSDATE,'MM'),-12)
  AND seller_id BETWEEN 10001 AND 10100;

SELECT * FROM TABLE(DBMS_XPLAN.DISPLAY_CURSOR(NULL,NULL,'ALLSTATS LAST +PEEKED_BINDS +PREDICATE'));
