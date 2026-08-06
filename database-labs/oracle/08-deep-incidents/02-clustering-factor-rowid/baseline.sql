CREATE INDEX ix_lab_order_status_date ON lab_order_header(status,created_at);
SELECT index_name,blevel,leaf_blocks,clustering_factor,num_rows FROM user_indexes WHERE index_name='IX_LAB_ORDER_STATUS_DATE';
SELECT /* cf_lab */ id,seller_id,customer_id,payload FROM lab_order_header WHERE status='PENDING' AND created_at>=SYSTIMESTAMP-INTERVAL '180' DAY;
SELECT * FROM TABLE(DBMS_XPLAN.DISPLAY_CURSOR(NULL,NULL,'ALLSTATS LAST +IOSTATS'));
