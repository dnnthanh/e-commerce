CREATE INDEX ix_lab_candidate INVISIBLE ON lab_order_header(seller_id,created_at DESC,status);
ALTER SESSION SET optimizer_use_invisible_indexes=TRUE;
SELECT /* spm_lab_candidate */ id,status,created_at FROM lab_order_header WHERE seller_id=999 ORDER BY created_at DESC FETCH FIRST 500 ROWS ONLY;
ALTER SESSION SET optimizer_use_invisible_indexes=FALSE;
-- If candidate wins across regression matrix, make visible in canary then consider SQL Plan Management only for proven regression containment.
