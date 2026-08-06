SET STATISTICS IO ON;
SET STATISTICS TIME ON;
DECLARE @customer_id bigint = 1;
SELECT TOP (100) id,order_no,status,total_amount,created_at
FROM order_header
WHERE customer_id=@customer_id
ORDER BY created_at DESC,id DESC;
-- Re-run with a long-tail customer and compare actual rows, memory grant and compiled parameter in the actual plan.
-- Evaluate Parameter Sensitive Plan/Query Store or query shape before using RECOMPILE everywhere.
