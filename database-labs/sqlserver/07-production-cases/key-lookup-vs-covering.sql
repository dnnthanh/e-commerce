SET STATISTICS IO ON;
SET STATISTICS TIME ON;

SELECT TOP (200) id,order_no,status,total_amount,created_at,shipping_address
FROM order_header
WHERE customer_id=@customer_id
ORDER BY created_at DESC,id DESC;

-- Inspect actual plan: seek + thousands of Key Lookup can be worse than a scan.
-- Candidate, apply only in lab:
-- CREATE INDEX IX_order_customer_created
-- ON order_header(customer_id,created_at DESC,id DESC)
-- INCLUDE(order_no,status,total_amount);
-- Deliberately do not INCLUDE a very wide shipping_address unless list API truly needs it.
