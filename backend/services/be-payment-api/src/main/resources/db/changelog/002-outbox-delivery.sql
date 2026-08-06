ALTER TABLE outbox_event
    ADD COLUMN attempt_count INT NOT NULL DEFAULT 0,
    ADD COLUMN locked_until TIMESTAMP NULL,
    ADD COLUMN next_attempt_at TIMESTAMP NULL,
    ADD COLUMN last_error TEXT NULL;
