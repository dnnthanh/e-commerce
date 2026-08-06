CREATE INDEX ix_lab_outbox_status_created ON lab_outbox(status,created_at,id);
SELECT id,aggregate_id FROM lab_outbox WHERE status='PENDING' ORDER BY created_at,id FETCH FIRST 100 ROWS ONLY FOR UPDATE SKIP LOCKED;
-- Keep transaction open in session A; run same claim in B/C and verify disjoint sets.
SELECT sid,event,seconds_in_wait,blocking_session FROM v$session WHERE username=USER;
