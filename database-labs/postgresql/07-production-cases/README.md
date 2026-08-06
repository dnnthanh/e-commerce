# PostgreSQL production troubleshooting cases

Đây là phần lab chính, không phải catalog cú pháp. Mỗi case mô phỏng một incident/performance review có thể gặp ở hệ thống marketplace chạy lâu năm.

## Quy trình bắt buộc cho mọi case

1. **Định nghĩa SLO/query contract**: query dùng ở API, worker hay reconciliation; bao nhiêu RPS; page đầu hay deep page; có cần total count không.
2. **Capture baseline** bằng `EXPLAIN (ANALYZE, BUFFERS, WAL, SETTINGS, VERBOSE)` và lưu plan thật dưới `04-plan-analysis/runtime/`.
3. **Khoanh node đắt** bằng `actual time`, `rows`, `loops`, temp blocks và buffer reads. Không đọc plan chỉ từ cost.
4. **So estimate/actual**. Sai cardinality 100x thường là statistics/data-correlation problem trước khi là index problem.
5. **Một hypothesis / một change**. Không vừa thêm 4 index vừa tăng `work_mem` rồi tuyên bố nhanh hơn.
6. **Đo read + write cost**. Index làm SELECT nhanh nhưng có thể làm import/checkout/outbox update chậm hơn.
7. **Regression với data skew khác**: hot seller vs long-tail seller, hot date vs archive date, cold-cache vs warm-cache.
8. **Decision**: production fix, mitigation tạm thời, hay reject optimization vì write/storage/complexity cost lớn hơn lợi ích.

## Case catalog

| # | Incident | Root-cause skill | Candidate techniques |
|---|---|---|---|
| 01 | Invoice report 30–40s, nhiều CTE/join | loops, join fan-out, materialization | pre-filter keys, join order, composite/partial index |
| 02 | `SELECT column` nhanh nhưng `SELECT *` treo | TOAST/wide rows, network serialization | projection, vertical split, lazy/detail endpoint |
| 03 | Pagination duplicated/missed khi cùng timestamp | unstable order + concurrent writes | `(created_at,id)` keyset |
| 04 | Planner chọn nested loop sai vì seller/category correlation | cardinality estimation | extended stats, MCV, ANALYZE |
| 05 | Operational rare-state query scan bảng lớn | low-frequency hot subset | partial + covering index |
| 06 | Hash/sort spill ra disk | memory grant / work_mem | row reduction, indexes, scoped work_mem |
| 07 | 8 outbox workers block nhau | queue claiming | `FOR UPDATE SKIP LOCKED`, partial index, idempotency |
| 08 | Checkout/inventory deadlock | inconsistent lock order | deterministic ordering, short transaction |
| 09 | Nhiều trigger/event cùng SKU enqueue trùng | logical serialization | advisory xact lock + active-row invariant |
| 10 | JSONB facet query chậm | operator/index mismatch | GIN operator classes |
| 11 | Promotion time-overlap check scan lớn | interval overlap | GiST range index/exclusion strategy |
| 12 | 2B ledger rows, 30-day scan | physical correlation | BRIN vs B-tree trade-off |
| 13 | Partition có nhưng query vẫn scan nhiều partition | missing partition predicate/sargability | pruning + pg_partman lifecycle |
| 14 | Table/index bloat, index-only scan vẫn heap-fetch | MVCC/vacuum/visibility | autovacuum tuning, fillfactor, REINDEX decision |
| 15 | Thêm index liên tục làm write throughput tụt | write amplification | index budget, usage stats, consolidation |
| 16 | Connection-pool exhaustion dù SQL không quá chậm | long transaction/external wait | transaction boundary, pg_stat_activity diagnosis |

## Cách chạy

```bash
# 1) tạo dataset lab riêng
psql -d performance_lab \
  -v product_rows=300000 \
  -v event_rows=2000000 \
  -f database-labs/postgresql/07-production-cases/00-bootstrap.sql

# 2) chạy từng incident
psql -d performance_lab \
  -f database-labs/postgresql/07-production-cases/01-cte-join-explosion.sql
```

Tăng `event_rows` lên 10–30 triệu khi máy đủ RAM/SSD. Không cần scale cực lớn để học nếu plan shape đã xuất hiện; mục tiêu là tái tạo **đúng failure mode**, không phải benchmark khoe số.

## Expected learning outcome

Sau phần này người học phải trả lời được không chỉ “tạo index nào”, mà cả:

- Vì sao planner chọn node này?
- Node nào thật sự tốn thời gian sau khi nhân `loops`?
- Estimate sai từ đâu?
- Fix này tối ưu page đầu hay mọi page?
- Có làm tăng WAL, lock duration, vacuum pressure hoặc storage không?
- Khi data distribution đổi, fix còn đúng không?
- Có nên tối ưu SQL, đổi API contract, precompute read model, hay chuyển workload sang search/analytics engine?
