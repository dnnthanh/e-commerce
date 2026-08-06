CREATE TABLE checkout_process_state (
  checkout_id VARCHAR(64) PRIMARY KEY,
  idempotency_key VARCHAR(128) NOT NULL UNIQUE,
  step VARCHAR(32) NOT NULL,
  promotion_reserved BOOLEAN NOT NULL DEFAULT FALSE,
  inventory_reserved BOOLEAN NOT NULL DEFAULT FALSE,
  payment_initiated BOOLEAN NOT NULL DEFAULT FALSE,
  order_id VARCHAR(64) NULL,
  completed_actions TEXT NOT NULL DEFAULT '',
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL
);
