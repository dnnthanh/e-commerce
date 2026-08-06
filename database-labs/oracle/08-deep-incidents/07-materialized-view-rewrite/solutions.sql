CREATE MATERIALIZED VIEW mv_lab_seller_month BUILD IMMEDIATE REFRESH COMPLETE ON DEMAND ENABLE QUERY REWRITE AS SELECT seller_id,TRUNC(created_at,'MM') month_key,COUNT(*) orders FROM lab_order_header GROUP BY seller_id,TRUNC(created_at,'MM');
ALTER SESSION SET query_rewrite_enabled=TRUE;
SELECT /* mv_lab */ seller_id,TRUNC(created_at,'MM') month_key,COUNT(*) orders FROM lab_order_header GROUP BY seller_id,TRUNC(created_at,'MM');
SELECT * FROM TABLE(DBMS_XPLAN.DISPLAY_CURSOR(NULL,NULL,'ALLSTATS LAST'));
