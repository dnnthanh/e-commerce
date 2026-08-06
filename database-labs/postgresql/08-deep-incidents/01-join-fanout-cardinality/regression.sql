SELECT seller_id,count(*) FROM lab_order_header GROUP BY seller_id ORDER BY count(*) DESC LIMIT 20;
SELECT relname,n_live_tup,n_dead_tup FROM pg_stat_user_tables WHERE relname LIKE 'lab_%';
SELECT indexrelname,idx_scan,pg_size_pretty(pg_relation_size(indexrelid)) FROM pg_stat_user_indexes WHERE relname IN ('lab_order_header','lab_order_line','lab_outbox') ORDER BY pg_relation_size(indexrelid) DESC;
-- Re-run baseline for seller 999 and three long-tail seller ids, then for 7/90/365-day windows. Compare total time, buffers, loops and estimate error.
