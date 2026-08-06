-- Practice on a lab copy only. Partitioning requires a partition function/scheme and is primarily useful for lifecycle/maintenance/pruning.
CREATE PARTITION FUNCTION pf_order_created_date(date) AS RANGE RIGHT FOR VALUES ('2026-01-01','2026-04-01','2026-07-01','2026-10-01');
CREATE PARTITION SCHEME ps_order_created_date AS PARTITION pf_order_created_date ALL TO ([PRIMARY]);
