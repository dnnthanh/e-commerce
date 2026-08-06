-- Use a lab copy: interval partitioning is appropriate for long-lived settlement history/retention, not every OLTP table.
CREATE TABLE settlement_history_lab (
  id NUMBER NOT NULL,
  seller_id NUMBER NOT NULL,
  period_end DATE NOT NULL,
  payable_amount NUMBER(19,2) NOT NULL
)
PARTITION BY RANGE(period_end)
INTERVAL(NUMTOYMINTERVAL(1,'MONTH'))
(PARTITION p_bootstrap VALUES LESS THAN (DATE '2026-01-01'));
