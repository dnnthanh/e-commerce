ALTER TABLE outbox_event ADD attempt_count INT NOT NULL CONSTRAINT df_order_outbox_attempt_count DEFAULT 0;
ALTER TABLE outbox_event ADD locked_until DATETIME2 NULL;
ALTER TABLE outbox_event ADD next_attempt_at DATETIME2 NULL;
ALTER TABLE outbox_event ADD last_error NVARCHAR(2000) NULL;
