CREATE INDEX ix_lab_order_seller_created ON lab_order_header(seller_id,created_at DESC,status);
BEGIN DBMS_STATS.GATHER_TABLE_STATS(USER,'LAB_ORDER_HEADER',method_opt=>'FOR COLUMNS SIZE 254 SELLER_ID',cascade=>TRUE); END;/
SELECT sql_id,child_number,is_bind_sensitive,is_bind_aware,executions,buffer_gets FROM v$sql WHERE sql_text LIKE '%acs_lab%';
-- Allow ACS to create child cursors when justified; avoid hard hints before proving stable distributions.
