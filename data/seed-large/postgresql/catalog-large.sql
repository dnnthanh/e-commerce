-- 500,000 products + 1,000,000 SKUs with deterministic marketplace skew.
-- Distribution markers: seller_rank, category_weight, published_ratio, hot_product.
WITH source AS (
  SELECT g,1000000+g AS product_id,
    CASE WHEN g%100<18 THEN 1 WHEN g%100<32 THEN 2 WHEN g%100<45 THEN 3 WHEN g%100<55 THEN 4
         WHEN g%100<65 THEN 5 WHEN g%100<73 THEN 6 WHEN g%100<80 THEN 7 WHEN g%100<87 THEN 8
         WHEN g%100<91 THEN 9 WHEN g%100<95 THEN 10 WHEN g%100<98 THEN 11 ELSE 12 END AS category_weight,
    CASE WHEN g%100<55 THEN 10001+(g%20) WHEN g%100<85 THEN 10021+(g%80) ELSE 10101+(g%400) END AS seller_rank,
    CASE WHEN g%1000<930 THEN 'PUBLISHED' WHEN g%1000<960 THEN 'DRAFT'
         WHEN g%1000<985 THEN 'SUSPENDED' ELSE 'ARCHIVED' END AS published_ratio,
    (g%100<8) AS hot_product,
    CASE WHEN g%100<60 THEN g%90 ELSE 90+(g%640) END AS age_days
  FROM generate_series(1,500000) g
), named AS (
  SELECT *,CASE category_weight
    WHEN 1 THEN (ARRAY['Nova X','Lumina Phone','Orbit S','Aeris One','Zenix Edge'])[1+(g%5)]
    WHEN 2 THEN (ARRAY['WorkMate Pro','Nova Studio','Kite Air','Zenix Book','Aeris Work'])[1+(g%5)]
    WHEN 3 THEN (ARRAY['Orbit Tab','Lumina Pad','Aeris Slate','Nova Tab','WorkMate Go'])[1+(g%5)]
    WHEN 4 THEN (ARRAY['Pulse Buds','Nimbus Sound','Orbit Pods','Aeris Headset','Nova Audio'])[1+(g%5)]
    WHEN 5 THEN (ARRAY['Zenix GaN','Nova Power','Linka Charge','Orbit Charger','Pulse Cable'])[1+(g%5)]
    WHEN 6 THEN (ARRAY['Kite Keys','Aeris Board','WorkMate Keys','Nova Type','Zenix Mechanical'])[1+(g%5)]
    WHEN 7 THEN (ARRAY['Kite Mouse','Aeris Click','Zenix Pointer','WorkMate Mouse','Nova Glide'])[1+(g%5)]
    WHEN 8 THEN (ARRAY['Vista Pro','Lumina View','WorkMate Display','Nova Vision','Orbit Screen'])[1+(g%5)]
    WHEN 9 THEN (ARRAY['Nimbus Hub','Lumina Home','Orbit Home','Nova Smart Hub','Linka Home'])[1+(g%5)]
    WHEN 10 THEN (ARRAY['Vista Cam','Nimbus Secure','Orbit Cam','Nova Cam','Lumina Watch'])[1+(g%5)]
    WHEN 11 THEN (ARRAY['Linka Mesh','Nimbus Router','Linka Switch','Nova WiFi','Orbit Mesh'])[1+(g%5)]
    ELSE (ARRAY['Vault SSD','Vault Drive','Zenix Storage','Nova Portable SSD','Orbit Storage'])[1+(g%5)] END AS family
  FROM source
)
INSERT INTO product(id,seller_id,category_id,name,description,status,version,created_at,updated_at)
SELECT product_id,seller_rank,category_weight,
  family||' '||CASE WHEN hot_product THEN 'Pro ' ELSE '' END||lpad((1+(g%24))::text,2,'0')||
  CASE category_weight WHEN 1 THEN ' · '||(ARRAY['128GB','256GB','512GB'])[1+(g%3)]
       WHEN 2 THEN ' · '||(ARRAY['16/512','32/1TB','64/2TB'])[1+(g%3)]
       WHEN 8 THEN ' · '||(ARRAY['24 FHD','27 QHD','32 4K'])[1+(g%3)]
       WHEN 12 THEN ' · '||(ARRAY['1TB','2TB','4TB'])[1+(g%3)] ELSE '' END,
  CASE category_weight WHEN 1 THEN 'OLED 120Hz, 5G/eSIM, camera stabilization, 12-month electronic warranty.'
       WHEN 2 THEN 'Productivity notebook with NVMe storage, USB-C charging and business warranty.'
       WHEN 3 THEN 'Tablet for study/media with pen and keyboard support.'
       WHEN 4 THEN 'Wireless audio with dual microphones and low-latency mode.'
       WHEN 5 THEN 'USB-C PD accessory with over-temperature and over-current protection.'
       WHEN 8 THEN 'Color-accurate display with ergonomic stand and USB-C connectivity.'
       ELSE 'Marketplace catalog item with seller-specific warranty and delivery SLA.' END,
  published_ratio,0,now()-make_interval(days=>age_days,hours=>(g*7)%24),now()
FROM named ON CONFLICT(id) DO NOTHING;

INSERT INTO sku(id,product_id,seller_sku,variant_name,active)
SELECT 2000000+((p.id-1000001)*2)+v.variant_no+1,p.id,
  'P'||p.id||'-'||CASE v.variant_no WHEN 0 THEN 'BASE' ELSE 'UP' END,
  CASE p.category_id WHEN 1 THEN CASE v.variant_no WHEN 0 THEN 'Đen · 128/256GB' ELSE 'Xanh · 256/512GB' END
       WHEN 2 THEN CASE v.variant_no WHEN 0 THEN '16GB · 512GB SSD' ELSE '32GB · 1TB SSD' END
       WHEN 3 THEN CASE v.variant_no WHEN 0 THEN 'WiFi' ELSE '5G' END
       WHEN 8 THEN CASE v.variant_no WHEN 0 THEN 'Standard stand' ELSE 'Ergo stand' END
       WHEN 12 THEN CASE v.variant_no WHEN 0 THEN '1TB' ELSE '2TB' END
       ELSE CASE v.variant_no WHEN 0 THEN 'Đen' ELSE 'Trắng' END END,
  NOT(p.status IN('ARCHIVED','SUSPENDED') AND v.variant_no=1)
FROM product p CROSS JOIN(VALUES(0),(1)) v(variant_no)
WHERE p.id BETWEEN 1000001 AND 1500000 ON CONFLICT(id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('product','id'),GREATEST((SELECT max(id) FROM product),1));
SELECT setval(pg_get_serial_sequence('sku','id'),GREATEST((SELECT max(id) FROM sku),1));

-- History dominates the live queue. PENDING is intentionally tiny for partial-index labs.
INSERT INTO outbox_event(event_id,aggregate_id,event_type,payload_json,status,created_at,processed_at)
SELECT gen_random_uuid()::text,(1000000+g)::text,
  CASE g%5 WHEN 0 THEN 'PRODUCT_PUBLISHED' WHEN 1 THEN 'PRODUCT_PRICE_REINDEX_REQUESTED' ELSE 'PRODUCT_CHANGED' END,
  jsonb_build_object('productId',1000000+g,'hotProduct',(g%100<8),'version',1+(g%20)),
  CASE WHEN g%1000<5 THEN 'PENDING' WHEN g%1000<8 THEN 'FAILED' ELSE 'PROCESSED' END,
  now()-make_interval(days=>g%45,hours=>CASE WHEN g%100<70 THEN 1+(g%6) ELSE g%24 END),
  CASE WHEN g%1000<8 THEN NULL ELSE now()-make_interval(days=>g%45,hours=>CASE WHEN g%100<70 THEN 1+(g%6) ELSE g%24 END)+make_interval(secs=>1+g%15) END
FROM generate_series(1,2000000) g;

-- Dynamic attributes for facet/JSONB labs. Two definitions per category, one/two values per product.
INSERT INTO product_attribute_definition(category_id,code,label,data_type,required)
SELECT c.category_id,v.code,v.label,v.data_type,v.required
FROM (SELECT generate_series(1,12) AS category_id) c
CROSS JOIN (VALUES
  ('tier','Phân khúc','STRING',true),
  ('spec','Thông số chính','STRING',false)
) v(code,label,data_type,required)
ON CONFLICT(category_id,code) DO NOTHING;

INSERT INTO product_attribute_value(product_id,attribute_definition_id,value_json)
SELECT p.id,d.id,
  CASE d.code
    WHEN 'tier' THEN jsonb_build_object('value',CASE WHEN p.id%100<15 THEN 'premium' WHEN p.id%100<55 THEN 'midrange' ELSE 'value' END)
    ELSE jsonb_build_object('value',CASE p.category_id
      WHEN 1 THEN (ARRAY['OLED-120Hz','OLED-90Hz','LCD-120Hz'])[1+(p.id%3)]
      WHEN 2 THEN (ARRAY['16GB-512GB','32GB-1TB','64GB-2TB'])[1+(p.id%3)]
      WHEN 8 THEN (ARRAY['FHD-100Hz','QHD-165Hz','4K-144Hz'])[1+(p.id%3)]
      ELSE (ARRAY['standard','plus','pro'])[1+(p.id%3)] END)
  END
FROM product p
JOIN product_attribute_definition d ON d.category_id=p.category_id
WHERE p.id BETWEEN 1000001 AND 1500000;
