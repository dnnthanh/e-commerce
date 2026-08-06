CREATE INDEX ix_order_seller_channel_created ON lab_order_header(seller_id,channel,created_at DESC);
EXPLAIN ANALYZE SELECT id FROM lab_order_header WHERE seller_id=999 AND channel='MOBILE' ORDER BY created_at DESC LIMIT 1000;
-- Only promote frequently queried stable JSON paths; every generated/indexed path increases write and schema maintenance cost.
