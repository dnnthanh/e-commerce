-- With READ_COMMITTED_SNAPSHOT/SNAPSHOT enabled, open a long transaction and read lab_order_header.
BEGIN TRAN; SELECT COUNT_BIG(*) FROM dbo.lab_order_header; -- keep open while another session updates rows
SELECT DB_NAME(database_id) db,version_store_reserved_page_count FROM sys.dm_db_file_space_usage;
SELECT * FROM sys.dm_tran_active_snapshot_database_transactions ORDER BY elapsed_time_seconds DESC;
