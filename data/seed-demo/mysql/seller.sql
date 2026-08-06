-- 12 seller/shop records matching catalog seller_id.
INSERT IGNORE INTO seller(id,code,display_name,status,created_at,updated_at) VALUES
(10001,'NOVA','Nova Official Store','ACTIVE',NOW(),NOW()),
(10002,'WORKMATE','WorkMate Flagship','ACTIVE',NOW(),NOW()),
(10003,'ORBIT','Orbit Digital','ACTIVE',NOW(),NOW()),
(10004,'LUMINA','Lumina Tech','ACTIVE',NOW(),NOW()),
(10005,'ZENIX','Zenix Official','ACTIVE',NOW(),NOW()),
(10006,'AERIS','Aeris Gear','ACTIVE',NOW(),NOW()),
(10007,'KITE','Kite Computing','ACTIVE',NOW(),NOW()),
(10008,'NIMBUS','Nimbus Home','ACTIVE',NOW(),NOW()),
(10009,'PULSE','Pulse Audio','ACTIVE',NOW(),NOW()),
(10010,'VISTA','Vista Display','ACTIVE',NOW(),NOW()),
(10011,'LINKA','Linka Network','ACTIVE',NOW(),NOW()),
(10012,'VAULT','Vault Storage','ACTIVE',NOW(),NOW());
INSERT IGNORE INTO shop(id,seller_id,slug,name,description,status,updated_at) VALUES
(11001,10001,'nova-official','Nova Official Store','Cửa hàng marketplace demo có lịch sử thay đổi và thông báo tới khách hàng.','ACTIVE',NOW()),
(11002,10002,'workmate-official','WorkMate Flagship','Cửa hàng marketplace demo có lịch sử thay đổi và thông báo tới khách hàng.','ACTIVE',NOW()),
(11003,10003,'orbit-official','Orbit Digital','Cửa hàng marketplace demo có lịch sử thay đổi và thông báo tới khách hàng.','ACTIVE',NOW()),
(11004,10004,'lumina-official','Lumina Tech','Cửa hàng marketplace demo có lịch sử thay đổi và thông báo tới khách hàng.','ACTIVE',NOW()),
(11005,10005,'zenix-official','Zenix Official','Cửa hàng marketplace demo có lịch sử thay đổi và thông báo tới khách hàng.','ACTIVE',NOW()),
(11006,10006,'aeris-official','Aeris Gear','Cửa hàng marketplace demo có lịch sử thay đổi và thông báo tới khách hàng.','ACTIVE',NOW()),
(11007,10007,'kite-official','Kite Computing','Cửa hàng marketplace demo có lịch sử thay đổi và thông báo tới khách hàng.','ACTIVE',NOW()),
(11008,10008,'nimbus-official','Nimbus Home','Cửa hàng marketplace demo có lịch sử thay đổi và thông báo tới khách hàng.','ACTIVE',NOW()),
(11009,10009,'pulse-official','Pulse Audio','Cửa hàng marketplace demo có lịch sử thay đổi và thông báo tới khách hàng.','ACTIVE',NOW()),
(11010,10010,'vista-official','Vista Display','Cửa hàng marketplace demo có lịch sử thay đổi và thông báo tới khách hàng.','ACTIVE',NOW()),
(11011,10011,'linka-official','Linka Network','Cửa hàng marketplace demo có lịch sử thay đổi và thông báo tới khách hàng.','ACTIVE',NOW()),
(11012,10012,'vault-official','Vault Storage','Cửa hàng marketplace demo có lịch sử thay đổi và thông báo tới khách hàng.','ACTIVE',NOW());
