EXPLAIN (ANALYZE,BUFFERS) SELECT seller_id,sum(gross) FROM lab_settlement WHERE date_trunc('month',business_date)=date_trunc('month',current_date) GROUP BY seller_id;
SELECT indexrelname,idx_scan,idx_tup_read,idx_tup_fetch,pg_size_pretty(pg_relation_size(indexrelid)) FROM pg_stat_user_indexes WHERE relname='lab_settlement';
