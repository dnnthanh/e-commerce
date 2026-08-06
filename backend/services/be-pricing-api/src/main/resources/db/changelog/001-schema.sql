-- Schema only. Performance indexes and partitions intentionally live in database-labs.
CREATE TABLE price_rule (id BIGSERIAL PRIMARY KEY, sku_id BIGINT NOT NULL, seller_id BIGINT NOT NULL, currency VARCHAR(3) NOT NULL, amount NUMERIC(19,2) NOT NULL, valid_from TIMESTAMP NOT NULL, valid_to TIMESTAMP NULL, priority INT NOT NULL, status VARCHAR(32) NOT NULL, created_at TIMESTAMP NOT NULL);
