ALTER SESSION SET statistics_level=ALL;
SELECT /* pga_lab */ h.seller_id,SUM(l.amount) amount,COUNT(*) c FROM lab_order_header h JOIN lab_order_line l ON l.order_id=h.id WHERE h.created_at>=SYSTIMESTAMP-INTERVAL '365' DAY GROUP BY h.seller_id ORDER BY amount DESC;
SELECT sql_id,operation_type,actual_mem_used,max_mem_used,tempseg_size,number_passes FROM v$sql_workarea WHERE sql_id=(SELECT sql_id FROM v$sql WHERE sql_text LIKE '%pga_lab%' FETCH FIRST 1 ROW ONLY);
