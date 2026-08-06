-- Deterministic catalog demo: 12 categories, 120 distinct products, 240 category-specific variants.
-- Explicit IDs keep cross-service demo files stable.
INSERT INTO category(id,parent_id,name,slug,active) VALUES
(1,NULL,'Điện thoại','dien-thoai',true),(2,NULL,'Laptop','laptop',true),
(3,NULL,'Máy tính bảng','may-tinh-bang',true),(4,NULL,'Tai nghe','tai-nghe',true),
(5,NULL,'Sạc & cáp','sac-cap',true),(6,NULL,'Bàn phím','ban-phim',true),
(7,NULL,'Chuột','chuot',true),(8,NULL,'Màn hình','man-hinh',true),
(9,NULL,'Nhà thông minh','nha-thong-minh',true),(10,NULL,'Camera','camera',true),
(11,NULL,'Thiết bị mạng','thiet-bi-mang',true),(12,NULL,'Lưu trữ','luu-tru',true)
ON CONFLICT DO NOTHING;

WITH product_seed AS (
  SELECT 1000 + g AS id, 1 + ((g - 1) % 12) AS category_id, 1 + ((g - 1) / 12) AS generation
  FROM generate_series(1,120) AS g
), named AS (
  SELECT id,category_id,generation,10000 + category_id AS seller_id,
    CASE category_id
      WHEN 1 THEN (ARRAY['Nova X','Lumina Phone','Orbit S','Aeris One','Zenix Edge'])[1 + ((generation - 1) % 5)]
      WHEN 2 THEN (ARRAY['WorkMate Pro','Nova Studio','Kite Air','Zenix Book','Aeris Work'])[1 + ((generation - 1) % 5)]
      WHEN 3 THEN (ARRAY['Orbit Tab','Lumina Pad','Aeris Slate','Nova Tab','WorkMate Go'])[1 + ((generation - 1) % 5)]
      WHEN 4 THEN (ARRAY['Pulse Buds','Nimbus Sound','Orbit Pods','Aeris Headset','Nova Audio'])[1 + ((generation - 1) % 5)]
      WHEN 5 THEN (ARRAY['Zenix GaN','Nova Power','Linka Charge','Orbit Charger','Pulse Cable'])[1 + ((generation - 1) % 5)]
      WHEN 6 THEN (ARRAY['Kite Keys','Aeris Board','WorkMate Keys','Nova Type','Zenix Mechanical'])[1 + ((generation - 1) % 5)]
      WHEN 7 THEN (ARRAY['Kite Mouse','Aeris Click','Zenix Pointer','WorkMate Mouse','Nova Glide'])[1 + ((generation - 1) % 5)]
      WHEN 8 THEN (ARRAY['Vista Pro','Lumina View','WorkMate Display','Nova Vision','Orbit Screen'])[1 + ((generation - 1) % 5)]
      WHEN 9 THEN (ARRAY['Nimbus Hub','Lumina Home','Orbit Home','Nova Smart Hub','Linka Home'])[1 + ((generation - 1) % 5)]
      WHEN 10 THEN (ARRAY['Vista Cam','Nimbus Secure','Orbit Cam','Nova Cam','Lumina Watch'])[1 + ((generation - 1) % 5)]
      WHEN 11 THEN (ARRAY['Linka Mesh','Nimbus Router','Linka Switch','Nova WiFi','Orbit Mesh'])[1 + ((generation - 1) % 5)]
      ELSE (ARRAY['Vault SSD','Vault Drive','Zenix Storage','Nova Portable SSD','Orbit Storage'])[1 + ((generation - 1) % 5)]
    END AS family
  FROM product_seed
)
INSERT INTO product(id,seller_id,category_id,name,description,status,version,created_at,updated_at)
SELECT id,seller_id,category_id,
  family || ' ' || CASE category_id
    WHEN 1 THEN (ARRAY['128GB','256GB','512GB'])[1 + ((generation - 1) % 3)]
    WHEN 2 THEN (ARRAY['14-inch 16GB','14-inch 32GB','16-inch 32GB'])[1 + ((generation - 1) % 3)]
    WHEN 3 THEN (ARRAY['WiFi 128GB','5G 256GB','WiFi 256GB'])[1 + ((generation - 1) % 3)]
    WHEN 4 THEN (ARRAY['ANC','Studio','Sport'])[1 + ((generation - 1) % 3)]
    WHEN 5 THEN (ARRAY['45W','65W','100W'])[1 + ((generation - 1) % 3)]
    WHEN 6 THEN (ARRAY['TKL','75%','Full-size'])[1 + ((generation - 1) % 3)]
    WHEN 7 THEN (ARRAY['Silent','Ergo','Pro'])[1 + ((generation - 1) % 3)]
    WHEN 8 THEN (ARRAY['24 FHD','27 QHD','32 4K'])[1 + ((generation - 1) % 3)]
    WHEN 9 THEN (ARRAY['Matter','Zigbee','WiFi'])[1 + ((generation - 1) % 3)]
    WHEN 10 THEN (ARRAY['2K','4K','Indoor'])[1 + ((generation - 1) % 3)]
    WHEN 11 THEN (ARRAY['AX3000','AX5400','2.5GbE'])[1 + ((generation - 1) % 3)]
    ELSE (ARRAY['1TB','2TB','4TB'])[1 + ((generation - 1) % 3)] END,
  CASE category_id
    WHEN 1 THEN 'Điện thoại chính hãng, màn hình OLED, eSIM/5G, bảo hành điện tử 12 tháng.'
    WHEN 2 THEN 'Laptop dành cho công việc, pin cả ngày, SSD NVMe và cổng kết nối đầy đủ.'
    WHEN 3 THEN 'Máy tính bảng phục vụ học tập, giải trí và ghi chú; hỗ trợ bàn phím rời.'
    WHEN 4 THEN 'Tai nghe không dây độ trễ thấp, micro kép và codec chất lượng cao.'
    WHEN 5 THEN 'Phụ kiện sạc có bảo vệ quá nhiệt/quá dòng, tương thích USB-C PD.'
    WHEN 6 THEN 'Bàn phím công thái học, hot-swap tùy phiên bản và kết nối đa thiết bị.'
    WHEN 7 THEN 'Chuột không dây cảm biến chính xác, pin lâu và hỗ trợ Bluetooth/2.4GHz.'
    WHEN 8 THEN 'Màn hình độ phủ màu cao, chân đế công thái học và cổng USB-C tùy phiên bản.'
    WHEN 9 THEN 'Thiết bị nhà thông minh tương thích automation, hỗ trợ điều khiển từ xa.'
    WHEN 10 THEN 'Camera bảo mật có phát hiện chuyển động, lưu trữ cục bộ và cảnh báo realtime.'
    WHEN 11 THEN 'Thiết bị mạng cho căn hộ/văn phòng, roaming mesh và quản trị tập trung.'
    ELSE 'Thiết bị lưu trữ tốc độ cao, phù hợp backup, media và dữ liệu công việc.' END,
  CASE WHEN generation = 10 AND category_id IN (2,8) THEN 'DRAFT'
       WHEN generation = 10 AND category_id IN (5,11) THEN 'SUSPENDED' ELSE 'PUBLISHED' END,
  0,now() - make_interval(days => (generation * 7 + category_id) % 90),now()
FROM named ON CONFLICT (id) DO NOTHING;

WITH sku_seed AS (
  SELECT 2001 + ((p.id - 1001) * 2) + v.variant_no AS id,p.id AS product_id,p.category_id,v.variant_no
  FROM product p CROSS JOIN (VALUES (0),(1)) AS v(variant_no)
  WHERE p.id BETWEEN 1001 AND 1120
)
INSERT INTO sku(id,product_id,seller_sku,variant_name,active)
SELECT id,product_id,'SKU-' || product_id || '-' || (variant_no + 1),
  CASE category_id
    WHEN 1 THEN CASE variant_no WHEN 0 THEN 'Đen · 128/256GB' ELSE 'Xanh · 256/512GB' END
    WHEN 2 THEN CASE variant_no WHEN 0 THEN '16GB RAM · 512GB SSD' ELSE '32GB RAM · 1TB SSD' END
    WHEN 3 THEN CASE variant_no WHEN 0 THEN 'WiFi · Xám' ELSE '5G · Bạc' END
    WHEN 4 THEN CASE variant_no WHEN 0 THEN 'Đen' ELSE 'Trắng' END
    WHEN 5 THEN CASE variant_no WHEN 0 THEN '1 cổng USB-C' ELSE '2 cổng USB-C' END
    WHEN 6 THEN CASE variant_no WHEN 0 THEN 'Brown switch' ELSE 'Silent red switch' END
    WHEN 7 THEN CASE variant_no WHEN 0 THEN 'Đen' ELSE 'Trắng' END
    WHEN 8 THEN CASE variant_no WHEN 0 THEN 'Chân tiêu chuẩn' ELSE 'Ergo arm' END
    WHEN 9 THEN CASE variant_no WHEN 0 THEN 'Matter' ELSE 'Zigbee' END
    WHEN 10 THEN CASE variant_no WHEN 0 THEN 'Bản trong nhà' ELSE 'Bản ngoài trời' END
    WHEN 11 THEN CASE variant_no WHEN 0 THEN '1-pack' ELSE '2-pack' END
    ELSE CASE variant_no WHEN 0 THEN '1TB' ELSE '2TB' END END,
  NOT (product_id IN (1120,1108) AND variant_no = 1)
FROM sku_seed ON CONFLICT (id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('category','id'), GREATEST((SELECT max(id) FROM category),1));
SELECT setval(pg_get_serial_sequence('product','id'), GREATEST((SELECT max(id) FROM product),1));
SELECT setval(pg_get_serial_sequence('sku','id'), GREATEST((SELECT max(id) FROM sku),1));
