SET track_io_timing=on;
SET work_mem='32MB';
EXPLAIN (ANALYZE,BUFFERS,WAL,SETTINGS,VERBOSE)
WITH recent_orders AS MATERIALIZED (
  SELECT id,seller_id,status,created_at FROM lab_order_header
  WHERE seller_id=999 AND created_at>=now()-interval '180 days'
), event_counts AS MATERIALIZED (
  SELECT aggregate_id,count(*) events FROM lab_outbox
  WHERE event_type='ORDER_CHANGED' GROUP BY aggregate_id
)
SELECT r.status,count(*) lines,sum(l.amount) amount,sum(coalesce(e.events,0)) event_refs
FROM recent_orders r
JOIN lab_order_line l ON l.order_id=r.id
LEFT JOIN event_counts e ON e.aggregate_id='O-'||r.id
GROUP BY r.status ORDER BY amount DESC;
-- Save CTE timings separately: a parent node elapsed time includes work from children; do not add every node duration as if independent.
