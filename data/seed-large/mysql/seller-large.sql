-- 500 operational sellers + shops/staff/history with Pareto-like volume tiers.
-- seller_tier_weight. Set-based generation avoids recursive CTE recursion/configuration surprises.
DROP TEMPORARY TABLE IF EXISTS seed_number;
CREATE TEMPORARY TABLE seed_number(n INT PRIMARY KEY);
INSERT INTO seed_number(n)
SELECT 1 + d0.n + 10*d1.n + 100*d2.n
FROM (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) d0
CROSS JOIN (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) d1
CROSS JOIN (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) d2
WHERE 1 + d0.n + 10*d1.n + 100*d2.n <= 500;

INSERT IGNORE INTO seller(id,code,display_name,status,created_at,updated_at)
SELECT 10000+n, CONCAT('SELLER-L-',LPAD(n,4,'0')),
       CONCAT(CASE WHEN n%5=0 THEN 'TechHub ' WHEN n%5=1 THEN 'Digital House ' WHEN n%5=2 THEN 'Smart Living ' WHEN n%5=3 THEN 'Office Gear ' ELSE 'Mobile Corner ' END,n),
       CASE WHEN n%100<2 THEN 'SUSPENDED' WHEN n%100<5 THEN 'VERIFIED' ELSE 'ACTIVE' END,
       NOW()-INTERVAL (30+MOD(n*17,900)) DAY,NOW()
FROM seed_number;

INSERT IGNORE INTO shop(seller_id,slug,name,description,status,updated_at)
SELECT s.id,LOWER(CONCAT('shop-',s.code)),s.display_name,
       CONCAT('Marketplace seller tier ',CASE WHEN MOD(s.id,100)<20 THEN 'A' WHEN MOD(s.id,100)<60 THEN 'B' ELSE 'C' END,
              ' với danh mục điện tử/phụ kiện và SLA vận hành riêng.'),
       CASE WHEN s.status='ACTIVE' THEN 'ACTIVE' ELSE 'INACTIVE' END,NOW()
FROM seller s WHERE s.id BETWEEN 10001 AND 10500;

-- seller_staff: 2-6 members/seller, permission mix differs by role.
INSERT IGNORE INTO seller_staff(seller_id,user_id,permissions_csv,active,updated_at)
SELECT s.id,CONCAT('seller-user-',s.id,'-',slot.n),
       CASE slot.n WHEN 1 THEN 'ORDER_READ,ORDER_WRITE,PRODUCT_WRITE,STAFF_ADMIN'
                   WHEN 2 THEN 'ORDER_READ,ORDER_WRITE,FULFILLMENT_WRITE'
                   WHEN 3 THEN 'PRODUCT_READ,PRODUCT_WRITE,PRICE_WRITE'
                   ELSE 'ORDER_READ,PRODUCT_READ' END,
       NOT(s.status='SUSPENDED' AND slot.n>2),NOW()
FROM seller s JOIN seed_number slot ON slot.n<=2+MOD(s.id,5)
WHERE s.id BETWEEN 10001 AND 10500 AND slot.n<=6;

-- seller_profile_history: top sellers change profile more often than long-tail sellers.
INSERT INTO seller_profile_history(seller_id,changed_by,before_json,after_json,changed_at)
SELECT s.id,CONCAT('seller-user-',s.id,'-1'),
       JSON_OBJECT('sequence',change_no.n-1,'tier',CASE WHEN MOD(s.id,100)<20 THEN 'A' ELSE 'B/C' END),
       JSON_OBJECT('sequence',change_no.n,'pickupSlaHours',4+MOD(change_no.n,20)),
       NOW()-INTERVAL MOD(change_no.n*7+s.id,730) DAY
FROM seller s JOIN seed_number change_no
  ON change_no.n<=CASE WHEN MOD(s.id,100)<20 THEN 100 WHEN MOD(s.id,100)<60 THEN 30 ELSE 8 END
WHERE s.id BETWEEN 10001 AND 10500 AND change_no.n<=100;

DROP TEMPORARY TABLE seed_number;
