# SQL Server production cases — order/fulfillment

Bật actual execution plan và:

```sql
SET STATISTICS IO ON;
SET STATISTICS TIME ON;
```

## Case 1 — Key lookup explosion

Order list lọc `customer_id`, sort `created_at,id`, nhưng SELECT nhiều columns làm hàng chục nghìn Key Lookup. Thử nonclustered index với key phục vụ filter/order và `INCLUDE` đúng response columns. Đo logical reads + write cost.

## Case 2 — Parameter sniffing / seller-customer skew

Stored query có customer cực lớn và customer bình thường. So sánh compiled parameter, actual rows, memory grant. Không mặc định dùng `OPTION(RECOMPILE)`; đánh giá PSP/Query Store/rewriting theo tần suất và CPU compile cost.

## Case 3 — Sort/hash spill và memory grant

Actual plan cảnh báo spill, tempdb I/O cao. Trước khi tăng memory, giảm rows, cải thiện cardinality, index ordering. Theo dõi granted/used memory.

## Case 4 — Deadlock order + fulfillment

Hai transaction lock order_header/order_line theo thứ tự khác nhau. Capture Extended Events deadlock graph, sửa deterministic lock order + transaction scope; không chữa bằng retry vô hạn.

## Case 5 — Deep OFFSET/FETCH

`OFFSET 200000 ROWS FETCH NEXT 50` vẫn đọc/bỏ nhiều rows. So sánh keyset predicate `(created_at,id)` tương đương bằng điều kiện OR phù hợp SQL Server và index `(customer_id,created_at DESC,id DESC)`.
