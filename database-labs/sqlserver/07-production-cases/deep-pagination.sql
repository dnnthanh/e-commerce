SET STATISTICS IO ON;
SET STATISTICS TIME ON;
SELECT id,order_no,status,total_amount,created_at
FROM order_header
WHERE customer_id=@customer_id
ORDER BY created_at DESC,id DESC
OFFSET 200000 ROWS FETCH NEXT 50 ROWS ONLY;

SELECT TOP (50) id,order_no,status,total_amount,created_at
FROM order_header
WHERE customer_id=@customer_id
  AND (created_at<@last_created_at OR (created_at=@last_created_at AND id<@last_id))
ORDER BY created_at DESC,id DESC;
-- Compare logical reads at page 1 vs deep page; keyset requires deterministic unique tie-breaker.
