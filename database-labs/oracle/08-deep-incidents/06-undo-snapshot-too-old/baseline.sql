-- Session A: long consistent report.
SET TRANSACTION READ ONLY;
SELECT /* undo_lab */ SUM(amount) FROM lab_order_line;
-- Session B continuously updates hot rows while A remains open.
SELECT begin_time,end_time,maxquerylen,tuned_undoretention,ssolderrcnt FROM v$undostat ORDER BY begin_time DESC FETCH FIRST 24 ROWS ONLY;
