-- 1,000,000 verified-purchase reviews with non-uniform rating and recency.
-- rating_weight, verified_ratio, review_recency_weight. Set-based to avoid million-row procedural loops.
DROP TEMPORARY TABLE IF EXISTS seed_number;
CREATE TEMPORARY TABLE seed_number(n INT PRIMARY KEY);
INSERT INTO seed_number(n)
SELECT 1 + d0.n + 10*d1.n + 100*d2.n + 1000*d3.n + 10000*d4.n + 100000*d5.n
FROM (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) d0
CROSS JOIN (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) d1
CROSS JOIN (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) d2
CROSS JOIN (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) d3
CROSS JOIN (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) d4
CROSS JOIN (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) d5
WHERE 1 + d0.n + 10*d1.n + 100*d2.n + 1000*d3.n + 10000*d4.n + 100000*d5.n <= 1000000;

-- verified_ratio is 100% because this table is the review-eligibility source of truth.
INSERT IGNORE INTO verified_purchase(user_id,order_line_id,product_id,sku_id,delivered_at)
SELECT CONCAT('customer-',LPAD(1+MOD(n,250000),7,'0')),9000000+n,
       1000001+MOD(n,500000),2000001+MOD(n,1000000),
       NOW()-INTERVAL (CASE WHEN MOD(n,100)<68 THEN MOD(n,90) ELSE 90+MOD(n,640) END + 2) DAY
FROM seed_number;

INSERT IGNORE INTO review(user_id,product_id,order_line_id,rating,title,content,status,created_at,updated_at)
SELECT CONCAT('customer-',LPAD(1+MOD(n,250000),7,'0')),1000001+MOD(n,500000),9000000+n,
       CASE WHEN MOD(n,100)<3 THEN 1 WHEN MOD(n,100)<8 THEN 2 WHEN MOD(n,100)<25 THEN 3 WHEN MOD(n,100)<62 THEN 4 ELSE 5 END AS rating_weight,
       CASE
         WHEN MOD(n,100)<3 THEN ELT(1+MOD(n,3),'Không phù hợp','Sản phẩm có vấn đề','Cần hỗ trợ')
         WHEN MOD(n,100)<8 THEN ELT(1+MOD(n,3),'Chưa như kỳ vọng','Cần cải thiện','Trải nghiệm chưa tốt')
         WHEN MOD(n,100)<25 THEN ELT(1+MOD(n,3),'Ổn nhưng còn điểm trừ','Tạm được','Đúng chức năng')
         WHEN MOD(n,100)<62 THEN ELT(1+MOD(n,4),'Tốt trong tầm giá','Dùng ổn','Giao nhanh','Khá hài lòng')
         ELSE ELT(1+MOD(n,4),'Rất hài lòng','Đáng tiền','Đúng mô tả','Sẽ mua lại') END,
       CASE
         WHEN MOD(n,100)<8 THEN 'Trải nghiệm chưa ổn định; mình đã liên hệ cửa hàng để được kiểm tra hoặc đổi trả.'
         WHEN MOD(n,100)<25 THEN 'Dùng được cho nhu cầu cơ bản, chất lượng hoàn thiện ở mức chấp nhận được.'
         WHEN MOD(n,100)<62 THEN 'Sản phẩm đúng mô tả và giao đúng hẹn; có vài chi tiết nhỏ nhưng tổng thể tốt.'
         ELSE 'Đóng gói chắc chắn, đúng phiên bản; dùng thực tế ổn định và hiệu năng đúng kỳ vọng.' END,
       CASE WHEN MOD(n,5000)=0 THEN 'HIDDEN' ELSE 'PUBLISHED' END,
       NOW()-INTERVAL (CASE WHEN MOD(n,100)<68 THEN MOD(n,90) ELSE 90+MOD(n,640) END) DAY,
       NOW()-INTERVAL (CASE WHEN MOD(n,100)<68 THEN MOD(n,90) ELSE 90+MOD(n,640) END) DAY
FROM seed_number;

-- A subset receives helpful votes; reports are intentionally rare.
INSERT IGNORE INTO review_helpful(review_id,user_id,created_at)
SELECT r.id,CONCAT('customer-',LPAD(1+MOD(s.n*17,250000),7,'0')),NOW()-INTERVAL MOD(s.n,30) DAY
FROM review r JOIN seed_number s ON s.n=MOD(r.order_line_id,1000000)+1
WHERE r.order_line_id BETWEEN 9000001 AND 10000000 AND MOD(s.n,8)=0;

INSERT IGNORE INTO review_report(review_id,reporter_user_id,reason,status,created_at)
SELECT r.id,CONCAT('customer-',LPAD(1+MOD(r.id*31,250000),7,'0')),
       CASE MOD(r.id,3) WHEN 0 THEN 'SPAM' WHEN 1 THEN 'OFF_TOPIC' ELSE 'ABUSIVE_LANGUAGE' END,
       'OPEN',NOW()-INTERVAL MOD(r.id,14) DAY
FROM review r
WHERE r.order_line_id BETWEEN 9000001 AND 10000000 AND MOD(r.order_line_id,2000)=0;

DROP TEMPORARY TABLE seed_number;
