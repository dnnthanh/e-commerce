# MySQL 8 production cases — seller/review

Dùng `EXPLAIN ANALYZE` và kiểm tra `rows`, loops, actual time; thêm `performance_schema`/slow query log khi cần workload evidence.

## Case 1 — Composite left-prefix vs fake “index từng cột”

Seller shop/review query có equality seller/product, status và order by time. So sánh 3 single-column indexes với một composite index khớp access pattern; đo write cost.

## Case 2 — Filesort + deep pagination

`LIMIT 50 OFFSET 200000` + filesort. Chuyển sang deterministic keyset `(created_at,id)` và composite index.

## Case 3 — Low-selectivity status

95% `PUBLISHED`/`APPROVED`: index riêng status có thể bị optimizer bỏ. Học đọc selectivity thay vì ép index.

## Case 4 — Lock wait/deadlock InnoDB

Review moderation/batch update lock records theo thứ tự khác nhau. Dùng `SHOW ENGINE INNODB STATUS`, performance_schema data_locks/data_lock_waits; sửa lock order và transaction duration.

## Case 5 — Covering index nhưng row quá rộng

List screen không nên SELECT body/content/blob lớn chỉ để “tiện”. So sánh narrow projection với `SELECT *`; cân nhắc detail endpoint trước khi thêm INCLUDE-equivalent columns vào index.
