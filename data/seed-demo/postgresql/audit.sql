INSERT INTO audit_event(event_id,actor_id,actor_type,action,resource_type,resource_id,source_service,trace_id,before_json,after_json,reason,occurred_at) VALUES
(gen_random_uuid()::text,'admin.demo','USER','SELLER_STATUS_CHANGED','SELLER','10001','be-seller-api','demo-trace-1','{"status":"PENDING"}','{"status":"ACTIVE"}','KYC approved',now()),
(gen_random_uuid()::text,'admin.demo','USER','OPERATIONS_RECOVERY_REQUESTED','PAYMENT','PAY-DEMO-UNKNOWN','be-operations-api','demo-trace-2',NULL,'{"action":"RECONCILE"}','Manual recovery',now());
