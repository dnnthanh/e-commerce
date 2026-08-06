USE order_db;
GO
-- Customer order history: composite + covering index.
CREATE INDEX IX_marketplace_order_user_created
ON marketplace_order(user_id,created_at DESC,id DESC)
INCLUDE(order_no,status,gross_amount,discount_amount,payable_amount,version);
GO

-- Filtered index: useful only when PAID is selective enough for the target workload.
CREATE INDEX IX_marketplace_order_paid_recent
ON marketplace_order(created_at DESC,id DESC)
INCLUDE(user_id,order_no,payable_amount)
WHERE status='PAID';
GO

-- Seller operational queue.
CREATE INDEX IX_seller_order_seller_status
ON seller_order(seller_id,status,id)
INCLUDE(order_id,seller_order_no,payable_amount);
GO

-- Join coverage for seller_order -> lines.
CREATE INDEX IX_order_line_seller_order
ON order_line(seller_order_id,sku_id)
INCLUDE(quantity,unit_price,allocated_discount,net_amount);
GO
