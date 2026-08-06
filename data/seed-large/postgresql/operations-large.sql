INSERT INTO operations_incident(incident_key,incident_type,source_service,recovery_target,aggregate_id,status,severity,last_error,last_recovery_message,first_seen_at,last_seen_at,resolved_at)
SELECT 'INC-L-'||g,CASE g%4 WHEN 0 THEN 'PAYMENT_UNKNOWN' WHEN 1 THEN 'DLT' WHEN 2 THEN 'SAGA_STUCK' ELSE 'JOB_FAILED' END,
'be-source-'||(g%20),'be-target-'||(g%20),'AGG-'||g,CASE WHEN g%10=0 THEN 'OPEN' ELSE 'RESOLVED' END,CASE WHEN g%100=0 THEN 'CRITICAL' ELSE 'MEDIUM' END,
'Generated failure '||g,NULL,now()-(g%90)*interval '1 day',now(),CASE WHEN g%10=0 THEN NULL ELSE now() END FROM generate_series(1,500000) g ON CONFLICT DO NOTHING;
