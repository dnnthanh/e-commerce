-- 500,000 seller settlements with tiered volume and refund-adjusted periods.
-- seller_tier_weight, refund_adjustment_ratio.
INSERT INTO settlement(settlement_no,seller_id,period_start,period_end,gross_amount,commission_amount,payable_amount,status,created_at,updated_at)
SELECT 'SET-L-'||LPAD(LEVEL,9,'0'),
  CASE WHEN MOD(LEVEL,100)<55 THEN 10001+MOD(LEVEL,20) WHEN MOD(LEVEL,100)<85 THEN 10021+MOD(LEVEL,80) ELSE 10101+MOD(LEVEL,400) END AS seller_tier_weight,
  ADD_MONTHS(TRUNC(SYSDATE,'MM'),-MOD(LEVEL,24)),ADD_MONTHS(TRUNC(SYSDATE,'MM'),1-MOD(LEVEL,24))-INTERVAL '1' SECOND,
  5000000+MOD(LEVEL*7919,250000000),
  ROUND((5000000+MOD(LEVEL*7919,250000000))*CASE WHEN MOD(LEVEL,100)<20 THEN 0.035 WHEN MOD(LEVEL,100)<85 THEN 0.055 ELSE 0.075 END,2),
  ROUND((5000000+MOD(LEVEL*7919,250000000))*(1-CASE WHEN MOD(LEVEL,100)<20 THEN 0.035 WHEN MOD(LEVEL,100)<85 THEN 0.055 ELSE 0.075 END)
        -CASE WHEN MOD(LEVEL,100)<7 THEN MOD(LEVEL*3571,2500000) ELSE 0 END,2) AS refund_adjustment_ratio,
  CASE WHEN MOD(LEVEL,1000)<8 THEN 'FAILED' WHEN MOD(LEVEL,100)<12 THEN 'PENDING' WHEN MOD(LEVEL,100)<42 THEN 'CALCULATED' ELSE 'PAID' END,
  SYSTIMESTAMP-NUMTODSINTERVAL(MOD(LEVEL,730),'DAY'),SYSTIMESTAMP
FROM dual CONNECT BY LEVEL<=500000;
COMMIT;


-- ledger_reconciliation: each settlement gets two seller-order lines and ledger facts.
INSERT INTO settlement_line(settlement_id,seller_order_id,gross_amount,commission_amount,payable_amount)
SELECT s.id,'SORD-L-'||LPAD(s.id,10,'0')||'-'||slot.n,
       ROUND(s.gross_amount/2,2),ROUND(s.commission_amount/2,2),ROUND(s.payable_amount/2,2)
FROM settlement s CROSS JOIN (SELECT 1 n FROM dual UNION ALL SELECT 2 FROM dual) slot
WHERE s.settlement_no LIKE 'SET-L-%'
  AND NOT EXISTS (SELECT 1 FROM settlement_line l WHERE l.seller_order_id='SORD-L-'||LPAD(s.id,10,'0')||'-'||slot.n);

INSERT INTO seller_settlement_ledger_entry(seller_id,source_event_id,entry_type,amount,reference,created_at)
SELECT s.seller_id,'SET-SALE-'||s.id,'SALE',s.gross_amount,s.settlement_no,s.created_at
FROM settlement s WHERE s.settlement_no LIKE 'SET-L-%'
  AND NOT EXISTS (SELECT 1 FROM seller_settlement_ledger_entry e WHERE e.source_event_id='SET-SALE-'||s.id);

INSERT INTO seller_settlement_ledger_entry(seller_id,source_event_id,entry_type,amount,reference,created_at)
SELECT s.seller_id,'SET-COMMISSION-'||s.id,'COMMISSION',-s.commission_amount,s.settlement_no,s.created_at
FROM settlement s WHERE s.settlement_no LIKE 'SET-L-%'
  AND NOT EXISTS (SELECT 1 FROM seller_settlement_ledger_entry e WHERE e.source_event_id='SET-COMMISSION-'||s.id);

-- About 7% of periods contain a refund adjustment so payable does not look artificially perfect.
INSERT INTO seller_settlement_ledger_entry(seller_id,source_event_id,entry_type,amount,reference,created_at)
SELECT s.seller_id,'SET-REFUND-'||s.id,'REFUND',-LEAST(2500000,MOD(s.id*3571,2500000)),s.settlement_no,s.created_at
FROM settlement s WHERE s.settlement_no LIKE 'SET-L-%' AND MOD(s.id,100)<7
  AND NOT EXISTS (SELECT 1 FROM seller_settlement_ledger_entry e WHERE e.source_event_id='SET-REFUND-'||s.id);
COMMIT;
