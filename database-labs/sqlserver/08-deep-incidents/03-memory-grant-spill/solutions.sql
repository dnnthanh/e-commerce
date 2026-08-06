UPDATE STATISTICS dbo.lab_order_header WITH FULLSCAN; UPDATE STATISTICS dbo.lab_order_line WITH FULLSCAN;
CREATE INDEX IX_lab_line_order_amount ON dbo.lab_order_line(order_id) INCLUDE(amount);
-- Test row-count/statistics fixes before MIN_GRANT_PERCENT/MAX_GRANT_PERCENT hints. Let memory grant feedback stabilize over repeated executions.
