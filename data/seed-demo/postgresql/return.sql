INSERT INTO return_request(return_key,order_id,user_id,status,reason,refundable_amount,created_at,updated_at)
VALUES ('RET-DEMO-001','ORD-DEMO-001','customer.demo','REQUESTED','Sản phẩm không phù hợp nhu cầu',12890000,now(),now()) ON CONFLICT DO NOTHING;
