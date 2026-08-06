SET search_path = perf_lab, public;
-- INCIDENT: reads improved over months, but write TPS/WAL/storage degraded as indexes accumulated.
SELECT relname,indexrelname,idx_scan,idx_tup_read,idx_tup_fetch
FROM pg_stat_user_indexes
WHERE schemaname='perf_lab'
ORDER BY idx_scan,indexrelname;

SELECT relname,
       pg_size_pretty(pg_relation_size(relid)) heap,
       pg_size_pretty(pg_indexes_size(relid)) indexes,
       n_tup_ins,n_tup_upd,n_tup_del,n_tup_hot_upd
FROM pg_stat_user_tables
WHERE schemaname='perf_lab'
ORDER BY pg_indexes_size(relid) DESC;

-- Benchmark a controlled insert batch before and after EACH candidate index, recording:
-- elapsed time, WAL bytes (EXPLAIN WAL for representative DML), index size, and the read query it protects.
-- Review overlapping indexes: (a,b,c) may make (a,b) redundant for some workloads, but uniqueness/order/opclass/INCLUDE differences matter.
-- Never drop an index from idx_scan=0 alone without a representative observation window and checking constraint ownership.
