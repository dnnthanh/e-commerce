SET STATISTICS IO ON;
SET STATISTICS TIME ON;
SELECT TOP (100) order_no, user_id, payable_amount, status, created_at
FROM marketplace_order
WHERE user_id = 'user-000100'
ORDER BY created_at DESC;
