EXPLAIN (ANALYZE, BUFFERS, WAL, SETTINGS, FORMAT TEXT)
SELECT id, event_id, aggregate_id, event_type, payload_json, created_at
FROM outbox_event
WHERE status = 'PENDING'
ORDER BY created_at
LIMIT 500;
