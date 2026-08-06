-- Schema only. Performance indexes and partitions intentionally live in database-labs.
CREATE TABLE promotion (id BIGSERIAL PRIMARY KEY, code VARCHAR(64) NOT NULL UNIQUE, name VARCHAR(255) NOT NULL, promotion_type VARCHAR(64) NOT NULL, stacking_group VARCHAR(64) NULL, priority INT NOT NULL, start_at TIMESTAMP NOT NULL, end_at TIMESTAMP NOT NULL, status VARCHAR(32) NOT NULL);
CREATE TABLE promotion_condition (id BIGSERIAL PRIMARY KEY, promotion_id BIGINT NOT NULL REFERENCES promotion(id), condition_type VARCHAR(64) NOT NULL, condition_json JSONB NOT NULL);
