-- Compare a broad index and the workload-specific partial index separately, not together.
CREATE INDEX idx_outbox_status_created_at ON outbox_event(status, created_at);
-- Alternative for a queue where historical PROCESSED rows dominate:
-- CREATE INDEX idx_outbox_pending_created_at ON outbox_event(created_at) WHERE status = 'PENDING';
