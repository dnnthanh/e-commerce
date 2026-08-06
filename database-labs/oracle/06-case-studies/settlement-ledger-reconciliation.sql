ALTER SESSION SET statistics_level=ALL;

-- Reconcile settlement header, seller-order lines and immutable ledger facts.
WITH line_rollup AS (
  SELECT settlement_id,
         SUM(gross_amount) line_gross,
         SUM(commission_amount) line_commission,
         SUM(payable_amount) line_payable
  FROM settlement_line
  GROUP BY settlement_id
), ledger_rollup AS (
  SELECT reference settlement_no,
         SUM(CASE WHEN entry_type='SALE' THEN amount ELSE 0 END) ledger_sales,
         -SUM(CASE WHEN entry_type='COMMISSION' THEN amount ELSE 0 END) ledger_commission,
         SUM(CASE WHEN entry_type='REFUND' THEN amount ELSE 0 END) ledger_refund
  FROM seller_settlement_ledger_entry
  GROUP BY reference
)
SELECT /*+ gather_plan_statistics */ s.id,s.settlement_no,s.seller_id,s.status,
       s.gross_amount,l.line_gross,g.ledger_sales,
       s.commission_amount,l.line_commission,g.ledger_commission,
       s.payable_amount,l.line_payable,g.ledger_refund
FROM settlement s
JOIN line_rollup l ON l.settlement_id=s.id
LEFT JOIN ledger_rollup g ON g.settlement_no=s.settlement_no
WHERE s.gross_amount<>l.line_gross
   OR s.commission_amount<>l.line_commission
   OR s.payable_amount<>l.line_payable
   OR s.gross_amount<>NVL(g.ledger_sales,0)
ORDER BY s.period_end DESC,s.id DESC
FETCH FIRST 200 ROWS ONLY;

CREATE INDEX lab_settlement_line_header_money
ON settlement_line(settlement_id,gross_amount,commission_amount,payable_amount);
CREATE INDEX lab_settlement_ledger_reference_type
ON seller_settlement_ledger_entry(reference,entry_type,created_at);

-- HAVING version: sellers whose ledger net differs materially during a period.
SELECT seller_id,
       SUM(CASE WHEN entry_type='SALE' THEN amount ELSE 0 END) sales,
       SUM(CASE WHEN entry_type IN('COMMISSION','REFUND') THEN amount ELSE 0 END) deductions
FROM seller_settlement_ledger_entry
WHERE created_at>=ADD_MONTHS(TRUNC(SYSDATE,'MM'),-3)
GROUP BY seller_id
HAVING ABS(SUM(amount))>10000000;

SELECT * FROM TABLE(DBMS_XPLAN.DISPLAY_CURSOR(NULL,NULL,'ALLSTATS LAST +PEEKED_BINDS +OUTLINE'));
