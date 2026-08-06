# Oracle production cases — settlement/finance

Các case này tập trung vào workload tài chính/settlement, nơi “query nhanh” chưa đủ: plan phải ổn định theo kỳ, seller skew, partition pruning và concurrency batch.

Luôn capture actual row-source statistics:

```sql
ALTER SESSION SET statistics_level=ALL;
-- query with /*+ gather_plan_statistics */
SELECT * FROM TABLE(DBMS_XPLAN.DISPLAY_CURSOR(NULL,NULL,'ALLSTATS LAST +PEEKED_BINDS +PARTITION +IOSTATS'));
```

## Case 1 — Seller/period skew làm plan bind-sensitive

Một seller lớn có hàng triệu ledger rows, seller thường chỉ vài nghìn. So sánh actual rows với E-Rows và xem bind peeking/adaptive cursor sharing trước khi ép hint.

```sql
SELECT /*+ gather_plan_statistics */ settlement_no, seller_id, period_start, period_end, status, payable_amount
FROM settlement
WHERE seller_id=:seller_id
  AND period_start>=:from_period
ORDER BY period_start DESC
FETCH FIRST 100 ROWS ONLY;
```

Candidate: composite index theo equality + range/order; histogram khi distribution thật sự skew. Regression phải chạy cả hot seller và long-tail seller.

## Case 2 — Partition pruning bị mất vì function trên partition key

So sánh `TRUNC(period_start)=:day` với range `period_start>=:day AND period_start<:day+1`. Xem `PSTART/PSTOP` trong DBMS_XPLAN. Partitioning chỉ có giá trị khi optimizer prune được.

## Case 3 — Reconciliation aggregate spill / TEMP pressure

Finance reconciliation thường join settlement, ledger và refund history rồi group theo seller/order. Theo dõi `A-Rows`, physical reads, temp space, hash join/workarea. Fix ưu tiên giảm row sớm và đúng join cardinality trước khi tăng PGA.

## Case 4 — Batch workers tranh cùng settlement rows

Dùng `FOR UPDATE SKIP LOCKED` cho one-of-N batch ownership. Đo lock waits; vẫn yêu cầu idempotency vì worker có thể chết sau external side effect.

## Case 5 — Index budget cho finance writes

Dùng `V$SEGMENT_STATISTICS`, `DBA_INDEX_USAGE`/monitoring phù hợp môi trường để đánh giá index read benefit so với redo/undo/storage. Không giữ index chỉ vì “có vẻ hữu ích”.
