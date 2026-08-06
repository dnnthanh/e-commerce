SELECT pg_size_pretty(pg_relation_size('brin_lab_settlement_date'::regclass)) brin_size,pg_size_pretty(pg_relation_size('ix_lab_settlement_seller_date'::regclass)) btree_size;
SELECT indexrelname,idx_scan FROM pg_stat_user_indexes WHERE relname='lab_settlement';
-- Insert 1M additional rows before/after candidate indexes and compare elapsed time + WAL bytes. Drop indexes with no measured workload value.
