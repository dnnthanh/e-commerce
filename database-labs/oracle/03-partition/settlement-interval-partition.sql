-- Interval partitioning lab copy: new monthly partitions are created automatically.
BEGIN
  EXECUTE IMMEDIATE 'DROP TABLE settlement_history_lab PURGE';
EXCEPTION WHEN OTHERS THEN IF SQLCODE!=-942 THEN RAISE; END IF;
END;
/
CREATE TABLE settlement_history_lab(
  id NUMBER NOT NULL,
  settlement_no VARCHAR2(64) NOT NULL,
  seller_id NUMBER NOT NULL,
  payable_amount NUMBER(19,2) NOT NULL,
  status VARCHAR2(32) NOT NULL,
  period_start TIMESTAMP NOT NULL,
  period_end TIMESTAMP NOT NULL
)
PARTITION BY RANGE(period_start)
INTERVAL(NUMTOYMINTERVAL(1,'MONTH'))
(
  PARTITION p_before_2025 VALUES LESS THAN(TIMESTAMP '2025-01-01 00:00:00')
);

INSERT INTO settlement_history_lab
SELECT id,settlement_no,seller_id,payable_amount,status,period_start,period_end FROM settlement;
COMMIT;

CREATE INDEX idx_settlement_history_seller_period
ON settlement_history_lab(seller_id,period_start DESC)
LOCAL;
