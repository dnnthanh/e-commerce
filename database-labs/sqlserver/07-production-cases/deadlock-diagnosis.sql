-- Capture the system_health/Extended Events deadlock graph first.
-- Session A locks order_header then order_line; Session B deliberately reverses order to reproduce.
-- Production fix: deterministic aggregate/row ordering and shorter transaction boundaries; bounded retry only after root cause is understood.
SELECT request_session_id,resource_type,request_mode,request_status,resource_description
FROM sys.dm_tran_locks
ORDER BY request_session_id;
