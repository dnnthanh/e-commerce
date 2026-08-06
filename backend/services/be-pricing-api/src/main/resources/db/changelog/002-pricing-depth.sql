ALTER TABLE price_rule ALTER COLUMN seller_id DROP NOT NULL;
ALTER TABLE price_rule ADD COLUMN channel VARCHAR(32) NULL;
ALTER TABLE price_rule ADD COLUMN price_source VARCHAR(32) NOT NULL DEFAULT 'SELLER';
CREATE TABLE price_rule_history (
  id BIGSERIAL PRIMARY KEY,
  price_rule_id BIGINT NOT NULL REFERENCES price_rule(id),
  action VARCHAR(64) NOT NULL,
  actor_id VARCHAR(64) NOT NULL,
  changed_at TIMESTAMP NOT NULL
);
