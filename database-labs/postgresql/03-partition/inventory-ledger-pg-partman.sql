-- Partition lab is for time-series ledger/history, not inventory_balance point lookups.
CREATE EXTENSION IF NOT EXISTS pg_partman WITH SCHEMA public;
-- Execute after moving the lab copy to a native RANGE-partitioned table.
-- SELECT partman.create_parent(p_parent_table => 'public.inventory_ledger_lab', p_control => 'created_at', p_interval => '1 month');
