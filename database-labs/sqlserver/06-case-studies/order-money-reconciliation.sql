USE order_db;
SET STATISTICS IO ON;
SET STATISTICS TIME ON;

-- Detect parent/seller/line monetary drift. Useful after partial cancel/refund allocation changes.
WITH seller_rollup AS (
  SELECT order_id,
         SUM(gross_amount) seller_gross,
         SUM(discount_amount) seller_discount,
         SUM(payable_amount) seller_payable
  FROM seller_order
  GROUP BY order_id
), line_rollup AS (
  SELECT so.order_id,
         SUM(ol.quantity*ol.unit_price) line_gross,
         SUM(ol.allocated_discount) line_discount,
         SUM(ol.net_amount) line_payable
  FROM seller_order so
  JOIN order_line ol ON ol.seller_order_id=so.id
  GROUP BY so.order_id
)
SELECT TOP (200) o.id,o.order_no,o.status,
       o.gross_amount,s.seller_gross,l.line_gross,
       o.discount_amount,s.seller_discount,l.line_discount,
       o.payable_amount,s.seller_payable,l.line_payable
FROM marketplace_order o
JOIN seller_rollup s ON s.order_id=o.id
JOIN line_rollup l ON l.order_id=o.id
WHERE o.gross_amount<>s.seller_gross OR s.seller_gross<>l.line_gross
   OR o.discount_amount<>s.seller_discount OR s.seller_discount<>l.line_discount
   OR o.payable_amount<>s.seller_payable OR s.seller_payable<>l.line_payable
ORDER BY o.created_at DESC,o.id DESC;

-- Supporting foreign-key/index path for the two GROUP BY stages.
CREATE INDEX lab_seller_order_order_money
ON seller_order(order_id) INCLUDE(gross_amount,discount_amount,payable_amount,status);
CREATE INDEX lab_order_line_seller_money
ON order_line(seller_order_id) INCLUDE(quantity,unit_price,allocated_discount,net_amount);

-- A second shape using HAVING to identify seller-order-level drift.
SELECT so.id,so.seller_order_no,so.payable_amount,SUM(ol.net_amount) line_payable
FROM seller_order so JOIN order_line ol ON ol.seller_order_id=so.id
GROUP BY so.id,so.seller_order_no,so.payable_amount
HAVING so.payable_amount<>SUM(ol.net_amount);
