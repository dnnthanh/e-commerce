MERGE INTO settlement s USING (SELECT 'SET-DEMO-001' settlement_no FROM dual) x ON (s.settlement_no=x.settlement_no)
WHEN NOT MATCHED THEN INSERT(settlement_no,seller_id,period_start,period_end,gross_amount,commission_amount,payable_amount,status,created_at,updated_at)
VALUES('SET-DEMO-001',10001,SYSTIMESTAMP-INTERVAL '7' DAY,SYSTIMESTAMP,12990000,649500,12340500,'CALCULATED',SYSTIMESTAMP,SYSTIMESTAMP);
