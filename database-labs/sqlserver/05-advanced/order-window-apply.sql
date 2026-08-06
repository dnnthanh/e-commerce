USE order_db;
GO
SET STATISTICS IO ON;
SET STATISTICS TIME ON;

-- Latest seller order per marketplace order plus customer running spend/rank.
WITH customer_order AS (
  SELECT o.*,
    ROW_NUMBER() OVER(PARTITION BY o.user_id ORDER BY o.created_at DESC,o.id DESC) AS customer_order_rank,
    SUM(o.payable_amount) OVER(PARTITION BY o.user_id ORDER BY o.created_at,o.id ROWS UNBOUNDED PRECEDING) AS running_spend
  FROM marketplace_order o
  WHERE o.created_at>=DATEADD(day,-180,SYSDATETIME())
)
SELECT TOP(500)
  c.user_id,c.order_no,c.status,c.payable_amount,c.customer_order_rank,c.running_spend,
  so.seller_order_no,so.seller_id,so.status AS seller_status,line_summary.line_count,line_summary.units
FROM customer_order c
CROSS APPLY(
  SELECT TOP(1) s.seller_order_no,s.seller_id,s.status,s.id
  FROM seller_order s
  WHERE s.order_id=c.id
  ORDER BY s.id DESC
) so
CROSS APPLY(
  SELECT count(*) AS line_count,sum(l.quantity) AS units
  FROM order_line l
  WHERE l.seller_order_id=so.id
) line_summary
WHERE c.customer_order_rank<=5
ORDER BY c.user_id,c.customer_order_rank;
GO
