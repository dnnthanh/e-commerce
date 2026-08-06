ALTER TABLE authorization_change_log ADD COLUMN change_id VARCHAR(36);
ALTER TABLE authorization_change_log ADD COLUMN status VARCHAR(32) NOT NULL DEFAULT 'APPLIED';
ALTER TABLE authorization_change_log ADD COLUMN attempt_count INT NOT NULL DEFAULT 0;
ALTER TABLE authorization_change_log ADD COLUMN last_error TEXT NULL;
ALTER TABLE authorization_change_log ADD COLUMN last_attempt_at TIMESTAMP NULL;
ALTER TABLE authorization_change_log ADD COLUMN applied_at TIMESTAMP NULL;
UPDATE authorization_change_log SET change_id = id::text WHERE change_id IS NULL;
ALTER TABLE authorization_change_log ALTER COLUMN change_id SET NOT NULL;
ALTER TABLE authorization_change_log ADD CONSTRAINT uq_authorization_change_id UNIQUE(change_id);
