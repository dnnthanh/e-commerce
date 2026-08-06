CREATE INDEX IX_marketplace_order_user_created
ON marketplace_order(user_id, created_at DESC)
INCLUDE(order_no, payable_amount, status);
