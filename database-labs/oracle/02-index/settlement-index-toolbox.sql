-- Database/user: marketplace settlement schema.
-- Seller period history.
CREATE INDEX idx_settlement_seller_period
ON settlement(seller_id,period_start DESC,status);

-- Function-based index: useful only if the query applies the same expression.
CREATE INDEX idx_settlement_no_upper
ON settlement(UPPER(settlement_no));

-- Ledger event/order lookup.
CREATE INDEX idx_settlement_ledger_seller_created
ON seller_settlement_ledger_entry(seller_id,created_at DESC,entry_type);
