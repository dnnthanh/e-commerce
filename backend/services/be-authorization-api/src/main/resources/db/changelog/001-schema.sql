-- Schema only. Performance indexes and partitions intentionally live in database-labs.
CREATE TABLE authorization_change_log (id BIGSERIAL PRIMARY KEY, user_id VARCHAR(64) NOT NULL, change_type VARCHAR(64) NOT NULL, changed_by VARCHAR(64) NOT NULL, changed_at TIMESTAMP NOT NULL, payload_json TEXT NOT NULL);
CREATE TABLE outbox_event (id BIGSERIAL PRIMARY KEY, event_id VARCHAR(36) NOT NULL UNIQUE, aggregate_id VARCHAR(128) NOT NULL, event_type VARCHAR(128) NOT NULL, payload_json TEXT NOT NULL, status VARCHAR(32) NOT NULL, created_at TIMESTAMP NOT NULL, processed_at TIMESTAMP NULL);
