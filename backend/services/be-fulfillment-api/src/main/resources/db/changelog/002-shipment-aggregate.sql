ALTER TABLE shipment ADD carrier_sequence BIGINT NOT NULL CONSTRAINT df_shipment_carrier_sequence DEFAULT 0;

CREATE TABLE shipment_tracking (
  id BIGINT IDENTITY PRIMARY KEY,
  shipment_id BIGINT NOT NULL REFERENCES shipment(id),
  provider_sequence BIGINT NOT NULL,
  status VARCHAR(32) NOT NULL,
  occurred_at DATETIME2 NOT NULL,
  source VARCHAR(32) NOT NULL,
  CONSTRAINT uq_shipment_tracking_sequence UNIQUE(shipment_id, provider_sequence)
);

CREATE TABLE shipment_carrier_request (
  id BIGINT IDENTITY PRIMARY KEY,
  idempotency_key VARCHAR(128) NOT NULL,
  processed_at DATETIME2 NOT NULL,
  CONSTRAINT uq_shipment_carrier_request UNIQUE(idempotency_key)
);
