SHOW ENGINE INNODB STATUS;
SELECT * FROM performance_schema.data_lock_waits;
SELECT * FROM performance_schema.data_locks;
-- Reproduce two moderation transactions updating reviews in reverse id order, then fix by deterministic ordering and short transactions.
