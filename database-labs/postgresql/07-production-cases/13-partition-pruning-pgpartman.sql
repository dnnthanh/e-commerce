-- Partitioning incident checklist rather than a blind CREATE PARTITION recipe.
-- 1) Verify every hot/history query carries the intended partition key.
-- 2) EXPLAIN and inspect Subplans Removed / selected partitions.
-- 3) Avoid wrapping partition key in non-sargable functions.

-- Bad for pruning/index usage patterns when business_date is timestamp:
-- WHERE date(created_at) = current_date
-- Better range predicate:
-- WHERE created_at >= current_date AND created_at < current_date + interval '1 day'

-- Production automation policy in this repository uses pg_partman:
-- SELECT partman.create_parent(
--   p_parent_table => 'public.inventory_sync_success_item',
--   p_control => 'business_date',
--   p_type => 'native',
--   p_interval => 'monthly',
--   p_premake => 6
-- );
-- UPDATE partman.part_config
-- SET infinite_time_partitions=true
-- WHERE parent_table='public.inventory_sync_success_item';

-- Measure partitioning for retention/maintenance/pruning benefits, not as a universal lookup-speed feature.
