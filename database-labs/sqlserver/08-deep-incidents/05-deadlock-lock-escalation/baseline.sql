-- Session A: update parent then lines.
BEGIN TRAN; UPDATE dbo.lab_order_header SET status='PENDING' WHERE id BETWEEN 1000 AND 8000; WAITFOR DELAY '00:00:10'; UPDATE dbo.lab_order_line SET qty=qty WHERE order_id BETWEEN 1000 AND 8000;
-- Session B should reverse the object order to reproduce a deadlock. Capture system_health deadlock XML.
SELECT request_session_id,resource_type,request_mode,request_status FROM sys.dm_tran_locks;
