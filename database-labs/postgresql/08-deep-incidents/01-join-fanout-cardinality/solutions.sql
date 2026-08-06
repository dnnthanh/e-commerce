CREATE INDEX CONCURRENTLY IF NOT EXISTS ix_lab_order_hot_recent ON lab_order_header(seller_id,created_at DESC,id) INCLUDE(status);
CREATE INDEX CONCURRENTLY IF NOT EXISTS ix_lab_line_order_amount ON lab_order_line(order_id) INCLUDE(amount);
CREATE INDEX CONCURRENTLY IF NOT EXISTS ix_lab_outbox_aggregate_event ON lab_outbox(aggregate_id,event_type);
ANALYZE lab_order_header; ANALYZE lab_order_line; ANALYZE lab_outbox;
-- Rewrite: restrict event aggregation to candidate order ids instead of aggregating all 2M events first.
EXPLAIN (ANALYZE,BUFFERS,VERBOSE)
WITH recent_orders AS NOT MATERIALIZED (
 SELECT id,status FROM lab_order_header WHERE seller_id=999 AND created_at>=now()-interval '180 days'
), line_totals AS (
 SELECT l.order_id,count(*) lines,sum(l.amount) amount FROM lab_order_line l JOIN recent_orders r ON r.id=l.order_id GROUP BY l.order_id
), event_counts AS (
 SELECT substring(o.aggregate_id from 3)::bigint order_id,count(*) events FROM lab_outbox o JOIN recent_orders r ON o.aggregate_id='O-'||r.id WHERE o.event_type='ORDER_CHANGED' GROUP BY o.aggregate_id
)
SELECT r.status,sum(lt.lines),sum(lt.amount),sum(coalesce(ec.events,0)) FROM recent_orders r JOIN line_totals lt ON lt.order_id=r.id LEFT JOIN event_counts ec ON ec.order_id=r.id GROUP BY r.status;
