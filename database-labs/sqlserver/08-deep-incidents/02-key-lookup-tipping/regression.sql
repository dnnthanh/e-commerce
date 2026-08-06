SELECT i.name,ius.user_seeks,ius.user_scans,ius.user_updates FROM sys.indexes i LEFT JOIN sys.dm_db_index_usage_stats ius ON i.object_id=ius.object_id AND i.index_id=ius.index_id AND ius.database_id=DB_ID() WHERE i.object_id=OBJECT_ID('dbo.lab_order_header');
-- Compare DML update count versus lookup savings at different status selectivities.
