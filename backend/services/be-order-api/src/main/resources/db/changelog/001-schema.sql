-- Schema only. Performance indexes and partitions intentionally live in database-labs.
CREATE TABLE marketplace_order (
  id BIGINT IDENTITY PRIMARY KEY,
  order_no VARCHAR(64) NOT NULL UNIQUE,
  checkout_key VARCHAR(128) NOT NULL UNIQUE,
  user_id VARCHAR(64) NOT NULL,
  gross_amount DECIMAL(19,2) NOT NULL,
  discount_amount DECIMAL(19,2) NOT NULL,
  payable_amount DECIMAL(19,2) NOT NULL,
  status VARCHAR(32) NOT NULL,
  cancellation_reason VARCHAR(32) NULL,
  version BIGINT NOT NULL DEFAULT 0,
  created_at DATETIME2 NOT NULL,
  updated_at DATETIME2 NOT NULL
);
CREATE TABLE seller_order (
  id BIGINT IDENTITY PRIMARY KEY,
  order_id BIGINT NOT NULL REFERENCES marketplace_order(id),
  seller_id BIGINT NOT NULL,
  seller_order_no VARCHAR(64) NOT NULL UNIQUE,
  gross_amount DECIMAL(19,2) NOT NULL,
  discount_amount DECIMAL(19,2) NOT NULL,
  payable_amount DECIMAL(19,2) NOT NULL,
  status VARCHAR(32) NOT NULL
);
CREATE TABLE order_line (
  id BIGINT IDENTITY PRIMARY KEY,
  seller_order_id BIGINT NOT NULL REFERENCES seller_order(id),
  sku_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  unit_price DECIMAL(19,2) NOT NULL,
  allocated_discount DECIMAL(19,2) NOT NULL,
  net_amount DECIMAL(19,2) NOT NULL
);
CREATE TABLE inbox_event (
  id BIGINT IDENTITY PRIMARY KEY,
  consumer_name VARCHAR(128) NOT NULL,
  event_id VARCHAR(36) NOT NULL,
  processed_at DATETIME2 NOT NULL,
  CONSTRAINT uq_order_inbox UNIQUE(consumer_name,event_id)
);
CREATE TABLE outbox_event (
  id BIGINT IDENTITY PRIMARY KEY,
  event_id VARCHAR(36) NOT NULL UNIQUE,
  aggregate_id VARCHAR(128) NOT NULL,
  event_type VARCHAR(128) NOT NULL,
  payload_json NVARCHAR(MAX) NOT NULL,
  status VARCHAR(32) NOT NULL,
  created_at DATETIME2 NOT NULL,
  processed_at DATETIME2 NULL
);
